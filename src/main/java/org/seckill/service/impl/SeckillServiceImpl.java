package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.*;

/**
 * Created by edison on 16/5/22.
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;


    // MD5盐值字符串，用于混淆MD5
    private final String slat = "sdfseifjfknsueflmn2ur9wfsf8fkanfliejfakfn";



    private String getMD5(long seckillId){
        String base = seckillId + "/" + slat;

        // spring生成md5的工具类，需要一个二进制
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());

        return md5;
    }


    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {

        // 优化点：缓存优化
        Seckill seckill = redisDao.getSeckill(seckillId);

        if(seckill == null){
            seckill = seckillDao.queryById(seckillId);
            if(seckill == null){
                return new Exposer(false,seckillId);
            } else {
                redisDao.putSeckill(seckill);
            }

        }

        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();

        // 当前系统时间
        Date nowTime = new Date();

        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        // 转化特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);

        return new Exposer(true,md5,seckillId);
    }

    @Override
    @Transactional
    /**
     * 使用注解事务控制事务方法的优点：
     * 1 ：开发团队达成一致约定，明确标注事务方法的编码规则
     * 2 ：保证事务方法的执行时间尽可能短，不要穿插其它网络操作，rpc/http请求（运行时间比较长），或者剥离到方法外面去（做一个上层方法剥离）
     * 3 ：不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制（mysql行级锁）
     *
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {

        if(md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }

        try{

            // 执行秒杀逻辑：减库存 + 记录购买行为
            Date nowTime = new Date();


            // 减库存成功，记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);

            // 唯一验证:seckllId,phone
            if(insertCount <= 0) {
                // 重复秒杀
                throw new RepeatKillException("seckill repeated");
            }else {
                // 减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId,nowTime);

                if(updateCount <= 0){
                    // 没有更新到记录,秒杀结束
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    // 秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
                }
            }

        }catch (SeckillCloseException e1) {
            throw e1;
        }catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            // 所有编译期异常 转化为运行时异常
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }
    }

    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {

        if(md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId,SeckillStatEnum.DATA_REWRITE);
        }

        Date killTime = new Date();

        Map<String,Object> map = new HashMap<String,Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);

        try{
            // 执行存储过程，给result赋值
            seckillDao.killByProcedure(map);

            // 获取result
            int result = MapUtils.getInteger(map,"result",-2);
            if(result == 1){
                SuccessKilled sk = successKilledDao.
                        queryByIdWithSeckill(seckillId, userPhone);

                return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,sk);
            } else {
                // 根据id获取秒杀状态
                return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
        }

    }
}

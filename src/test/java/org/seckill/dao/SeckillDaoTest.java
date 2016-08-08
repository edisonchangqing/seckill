package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 配置spring和junit整合，junit启动时加载pringIOC容器
 * Created by edison on 16/5/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
// 告诉junitspring的配置文件位置
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {


    // 注入DAO实现类依赖
    @Resource
    private SeckillDao seckillDao;


    @Test
    public void testQueryById() throws Exception {

        long id = 1000;

        Seckill seckill = seckillDao.queryById(id);

        System.out.println(seckill.getName());

        System.out.println(seckill.toString());

    }

    @Test
    public void testQueryAll() throws Exception {

        // java 没有保存形参的的记录：queryAll(int offset,int limit) -> queryAll(arg0,arg1)
        List<Seckill> seckills = seckillDao.queryAll(0, 100);

        for(Seckill seckill : seckills){
             System.out.println(seckill.toString());
        }

    }


    @Test
    public void testReduceNumber() throws Exception {

        Date killTime = new Date();

        int updateCount = seckillDao.reduceNumber(1000L,killTime);

        System.out.println("updateCount="+updateCount);

    }


}

package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by edison on 16/6/9.
 */
public class RedisDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String ip,int port){
        jedisPool = new JedisPool(ip,port);
    }


    public Seckill getSeckill(long seckillId){
        // redis操作逻辑
        try{
            Jedis jedis = jedisPool.getResource();


            try{
                String key = "seckill:"+seckillId;

                // 并没有实现内部序列化操作
                // get -> byte[] —> 反序列化 ->Object(Seckill)
                // 采用自定义序列化
                byte[] bytes = jedis.get(key.getBytes());
                // 缓存中获取到
                if(bytes != null) {
                    // 空对象
                    Seckill seckill = schema.newMessage();
                    // seckill 被赋值 反序列化
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    return seckill;
                }


            }finally {
                jedis.close();
            }

        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }


    public String putSeckill(Seckill seckill){

        // object(Seckill) -> 字节数组
        try{
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "seckill:"+seckill.getSeckillId();

                byte [] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

                int timeOut = 60*60;
                String result = jedis.setex(key.getBytes(),timeOut,bytes);


                return result;

            }finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        return null;
    }



}

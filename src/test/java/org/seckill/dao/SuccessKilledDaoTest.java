package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by edison on 16/5/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

    @Resource
    private SuccessKilledDao successKilledDao;


    @Test
    public void testInsertSuccessKilled() throws Exception{

        Long id = 1001L;
        Long phone = 18701638177L;

        int insertCount = successKilledDao.insertSuccessKilled(id,phone);


        System.out.println("insertCount =" + insertCount);

    }


    @Test
    public void testQueryByIdWithSeckill() throws Exception{

        long id = 1001L;
        Long phone = 18701638177L;

        SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(id, phone);

        System.out.println(successKilled);
        System.out.println(successKilled.getSeckill());


    }


}

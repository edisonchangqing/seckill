package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by edison on 16/5/25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml",
"classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;


    @Test
    public void testGetSeckillList() throws Exception{

        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list={}",list);

    }

    @Test
    public void testGetById() throws Exception {

        long id = 1000;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}",seckill);

    }

    @Test
    public void testExportSeckillUrl() {

        // exposer=Exposer{exposed=true, md5='71edc71beefb2280b572c5c275f9b1db', seckillId=1000, now=0, start=0, end=0}

        long id = 1000;
        Exposer exposer = seckillService.exportSeckillUrl(id);

        logger.info("exposer={}",exposer);

    }

    @Test
    public void testExecuteSeckill() {
        long id = 1000;
        long phone = 12502171129L;
        String md5 = "71edc71beefb2280b572c5c275f9b1db";

        SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);

        logger.info("result={}",seckillExecution);

    }

}

--秒杀存储过程
DELIMITER $$ --console ;转换为 $$
--定义存储过程
--参数：in表示输入参数；out用于输出参数
--row_count():返回上一条修改类型sql的受影响的行数(update,delete,insert)
--row_count结果：0 未修改数据 >0 表示修改的行数 <0 表示sql错误或者未执行
CREATE PROCEDURE `seckill`.`execute_seckill`
 (in v_seckill_id bigint,in v_phone bigint,
    in v_kill_time TIMESTAMP ,out r_result int)
    BEGIN
      DECLARE insert_count int DEFAULT 0;
      START TRANSACTION ;
      insert ignore into success_killed
        (seckill_id,user_phone,create_time)
        VALUES (v_seckill_id,v_phone,v_kill_time);
        SELECT ROW_COUNT () INTO insert_count;
        IF (insert_count = 0) THEN
          ROLLBACK ;
          set r_result = -1;
        ELSEIF (insert_count < 0) THEN
          ROLLBACK ;
          SET r_result = -2;
        ELSE
          UPDATE seckill
            SET number = number - 1
          WHERE seckill_id = v_seckill_id
          AND end_time > v_kill_time
          AND start_time < v_kill_time
          AND number > 0;
          SELECT row_count() INTO insert_count;
          IF (insert_count = 0) THEN
            ROLLBACK ;
            set r_result = 0;
          ELSEIF (insert_count < 0) THEN
            ROLLBACK ;
            SET r_result = -2;
          ELSE
            COMMIT ;
            set r_result = 1;
          END IF ;
        END IF ;
      END ;
$$
-- 存储过程定义结束


SHOW create PROCEDURE execute_seckill\G;

-- 执行存储过程
DELIMITER ;

SET @r_result = -3;


call execute_seckill(1000,1350217881,now(),@r_result);

-- 获取结果
select @r_result;


-- 存储过程优化了事务行级锁的持有时间，一般银行会大量使用存储过程，互联网公司一般使用比较少；
-- 使用存储过程将事务放到mysql端执行





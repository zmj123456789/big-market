package cn.zmj.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;
/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动sku持久化对象
 * @create 2024-03-16 10:54
 */

@Data
public class RaffleActivitySku {
    /** 商品sku */
    private Long sku;
    /** 活动ID */
    private Long activityId;
    /** 活动个人参数ID；在这个活动上，一个人可参与多少次活动（总、日、月） */
    private Long activityCountId;
    /** 库存总量 */
    private Integer stockCount;
    /** 剩余库存 */
    private Integer stockCountSurplus;
    private Date createTime;
    private Date updateTime;
}

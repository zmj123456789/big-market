package cn.zmj.trigger.api.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class UserActivityAccountRequestDTO {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

}

package cn.zmj.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 活动账户实体对象
 * @create 2024-03-16 10:43
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityAccountEntity {
    private Long userId;
    private Long activityId;
    private Integer totalCount;
    private Integer totalCountSurplus;
    private Integer dayCount;
    private Integer dayCountSurplus;
    private Integer monthCount;
    private Integer monthCountSurplus;
}

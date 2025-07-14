package cn.zmj.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleAwardListResponseDTO {
    private Integer awardId;
    private String awardTitle;
    private String awardSubtitle;
    private Integer sort;
//    奖品次数规则，抽奖N次后解锁,未配置则为空
    private Integer awardRuleLockCount;
//    奖品是否解锁
    private Boolean isAwardUnlock;
//    等待解锁次数-规定抽奖N次解锁减去用户已经抽奖次数
    private Integer waitUnLockCount;


}

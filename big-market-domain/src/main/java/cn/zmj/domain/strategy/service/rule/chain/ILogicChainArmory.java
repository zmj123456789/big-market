package cn.zmj.domain.strategy.service.rule.chain;

public interface ILogicChainArmory {
    ILogiChain next();
    ILogiChain appendNext(ILogiChain next);
}

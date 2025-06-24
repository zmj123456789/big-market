package cn.zmj.domain.strategy.service.rule.chain;

public abstract class AbstractLogicChain implements  ILogiChain{
    private ILogiChain next;

    @Override
    public ILogiChain next() {
        return next;
    }

    @Override
    public ILogiChain appendNext(ILogiChain next) {
        this.next=next;
        return next;
    }
    protected abstract String ruleModel();
}

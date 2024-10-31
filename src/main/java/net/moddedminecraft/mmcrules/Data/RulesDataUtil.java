package net.moddedminecraft.mmcrules.Data;


public class RulesDataUtil {

    protected String rule, desc;

    public RulesDataUtil(String rule, String desc) {
        this.rule = rule;
        this.desc = desc;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

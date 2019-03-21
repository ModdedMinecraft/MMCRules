package net.moddedminecraft.mmcrules.Data;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.List;

public class RulesData extends RulesDataUtil{

    public RulesData(String rule, String desc) {
        super(rule, desc);
    }

    public static class RulesDataSerializer implements TypeSerializer<RulesData> {
        @SuppressWarnings("serial")
        final public static TypeToken<List<RulesData>> token = new TypeToken<List<RulesData>>() {};

        @Override
        public RulesData deserialize(TypeToken<?> token, ConfigurationNode node) throws ObjectMappingException {
            return new RulesData(
                    node.getNode("rule").getString(),
                    node.getNode("desc").getString());
        }

        @Override
        public void serialize(TypeToken<?> token, RulesData rulesData, ConfigurationNode node) throws ObjectMappingException {
            node.getNode("rule").setValue(rulesData.rule);
            node.getNode("desc").setValue(rulesData.desc);
        }
    }
}

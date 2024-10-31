package net.moddedminecraft.mmcrules.Data;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class RulesData extends RulesDataUtil {

    public RulesData(String rule, String desc) {
        super(rule, desc);
    }

    public static class RulesDataSerializer implements TypeSerializer<RulesData> {

        public static TypeToken<RulesData> token = new TypeToken<RulesData>() {};

        @Override
        public RulesData deserialize(Type type, ConfigurationNode data) {
            return new RulesData(
                    data.node("rule").getString(),
                    data.node("desc").getString());
        }

        @Override
        public void serialize(Type type, RulesData rulesData, ConfigurationNode data) throws SerializationException {
            data.node("rule").set(rulesData.rule);
            data.node("desc").set(rulesData.desc);
        }
    }
}

package cool.muyucloud.pullup.util;

import cool.muyucloud.pullup.util.condition.Condition;
/*? if >=26.1 {*/
/*import net.minecraft.resources.Identifier;*/
/*?} else {*/
import net.minecraft.util.Identifier;
/*?}*/
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;

import java.util.Collection;
import java.util.HashMap;

public class Registry<T> {
    public static final Registry<Function> FUNCTIONS = new Registry<>();
    public static final Registry<Argument> ARGUMENTS = new Registry<>();
    public static final Registry<Condition> CONDITIONS = new Registry<>();
    public static final Registry<Operator> OPERATORS = new Registry<>();

    public static void registerArguments() {
        ARGUMENTS.register(PlatformIds.parse("pullup:absolute_height"), snapshot -> snapshot.absoluteHeight());
        ARGUMENTS.register(PlatformIds.parse("pullup:relative_height"), snapshot -> snapshot.relativeHeight());
        ARGUMENTS.register(PlatformIds.parse("pullup:speed"), snapshot -> snapshot.speed());
        ARGUMENTS.register(PlatformIds.parse("pullup:horizontal_speed"), snapshot -> snapshot.horizontalSpeed());
        ARGUMENTS.register(PlatformIds.parse("pullup:vertical_speed"), snapshot -> snapshot.verticalSpeed());
        ARGUMENTS.register(PlatformIds.parse("pullup:yaw"), snapshot -> snapshot.yaw());
        ARGUMENTS.register(PlatformIds.parse("pullup:delta_yaw"), snapshot -> snapshot.deltaYaw());
        ARGUMENTS.register(PlatformIds.parse("pullup:pitch"), snapshot -> snapshot.pitch());
        ARGUMENTS.register(PlatformIds.parse("pullup:delta_pitch"), snapshot -> snapshot.deltaPitch());
        ARGUMENTS.register(PlatformIds.parse("pullup:distance_ahead"), snapshot -> snapshot.distanceAhead());
        ARGUMENTS.register(PlatformIds.parse("pullup:distance_horizontal"), snapshot -> snapshot.distanceHorizontal());
        ARGUMENTS.register(PlatformIds.parse("pullup:flight_ticks"), snapshot -> snapshot.flightTicks());
    }

    public static void registerOperators() {
        OPERATORS.register(PlatformIds.parse("pullup:gt"), new Operator(">", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] > args[1] ? 1 : -1;
            }
        });
        OPERATORS.register(PlatformIds.parse("pullup:geq"), new Operator(">=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] >= args[1] ? 1 : -1;
            }
        });
        OPERATORS.register(PlatformIds.parse("pullup:lt"), new Operator("<", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] < args[1] ? 1 : -1;
            }
        });
        OPERATORS.register(PlatformIds.parse("pullup:leq"), new Operator("<=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] <= args[1] ? 1 : -1;
            }
        });
        OPERATORS.register(PlatformIds.parse("pullup:eq"), new Operator("==", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] == args[1] ? 1 : -1;
            }
        });
        OPERATORS.register(PlatformIds.parse("pullup:neq"), new Operator("!=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] == args[1] ? -1 : 1;
            }
        });
        OPERATORS.register(PlatformIds.parse("pullup:and"), new Operator("&", 2, true, Operator.PRECEDENCE_ADDITION - 2) {
            @Override
            public double apply(double... args) {
                return args[0] >= 0 && args[1] >= 0 ? 1 : -1;
            }
        });
        OPERATORS.register(PlatformIds.parse("pullup:or"), new Operator("|", 2, true, Operator.PRECEDENCE_ADDITION - 3) {
            @Override
            public double apply(double... args) {
                return args[0] >= 0 || args[1] >= 0 ? 1 : -1;
            }
        });
    }

    public static void registerFunctions() {
        FUNCTIONS.register(PlatformIds.parse("pullup:pitched_distance"), new Function("pDistance") {
            @Override
            public double apply(double... args) {
                return 0;
            }
        });
    }

    private final HashMap<Identifier, T> registries = new HashMap<>();

    public void register(Identifier id, T content) {
        this.registries.put(id, content);
    }

    public T get(Identifier name) {
        return this.registries.get(name);
    }

    /**
     * Not a copy! Please do not modify.
     * Provide convenience for mixin class to execute.
     */
    public Collection<T> getAll() {
        return this.registries.values();
    }

    public void clear() {
        this.registries.clear();
    }

    public boolean has(Identifier id) {
        return this.registries.containsKey(id);
    }
}

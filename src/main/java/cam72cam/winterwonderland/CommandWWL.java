package cam72cam.winterwonderland;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.ConfigManager;

public class CommandWWL extends CommandBase {
	@Override
	public String getName() {
		return "winterwonderland";
	}

	@Override
	public List<String> getAliases() {
		ArrayList<String> list = new ArrayList<>();
		list.add("wwl");
		return list;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/winterwonderland";
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, "config", "accumulate", "melt");
		}

		if (args.length == 2) {
			switch (args[0]) {
				case "config": {
					List<String> list = Arrays.stream(Config.class.getFields()).map(Field::getName).collect(Collectors.toList());
					return getListOfStringsMatchingLastWord(args, list);
				}
			}
		}

		return Collections.emptyList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1) {
			throw new SyntaxErrorException("Not enough arguments");
		}

		switch (args[0]) {
			case "config": {
				try {
					if (args.length == 1) {
						for (Field field : Config.class.getFields())
							sender.sendMessage(new TextComponentString(field.getName() + " = " + field.get(null)));
					} else if (args.length < 3) {
						throw new SyntaxErrorException("Not enough arguments");
					} else {
						Field field = Arrays.stream(Config.class.getFields()).filter(f -> f.getName().equals(args[1])).findFirst().orElseThrow(() -> new SyntaxErrorException("No such field"));

						int value;
						net.minecraftforge.common.config.Config.RangeInt rangeInt = field.getAnnotation(net.minecraftforge.common.config.Config.RangeInt.class);
						if (rangeInt != null) {
							value = parseInt(args[2], rangeInt.min(), rangeInt.max());
						} else {
							value = parseInt(args[2]);
						}

						field.set(null, value);
						ConfigManager.sync(WinterWonderLand.MODID, net.minecraftforge.common.config.Config.Type.INSTANCE);
						sender.sendMessage(new TextComponentString("Set " + field.getName() + " to " + args[2]));
					}
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			}
			break;
			case "accumulate": {
				WinterWonderLand.accumulateMode = !WinterWonderLand.accumulateMode;
				WinterWonderLand.meltMode = false;
				if (WinterWonderLand.accumulateMode)
					sender.sendMessage(new TextComponentString("Started rapidly accumulating snow"));
				else
					sender.sendMessage(new TextComponentString("Stopped rapidly accumulating snow"));
			}
			break;
			case "melt": {
				WinterWonderLand.meltMode = !WinterWonderLand.meltMode;
				WinterWonderLand.accumulateMode = false;
				if (WinterWonderLand.meltMode)
					sender.sendMessage(new TextComponentString("Started rapidly melting snow"));
				else
					sender.sendMessage(new TextComponentString("Stopped rapidly melting snow"));
			}
			break;
			default:
				throw new SyntaxErrorException("Invalid argument");
		}
	}
}

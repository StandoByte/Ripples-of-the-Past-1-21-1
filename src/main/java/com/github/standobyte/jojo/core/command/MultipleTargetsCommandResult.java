package com.github.standobyte.jojo.core.command;

import java.util.Collection;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class MultipleTargetsCommandResult {
	public final Success success;
	public final Fail fail;
	
	public MultipleTargetsCommandResult(String key) {
		this.success = new Success(key + ".success");
		this.fail = new Fail(key + ".failed");
	}
	
	public MultipleTargetsCommandResult(String key, Fail fail) {
		this.success = new Success(key + ".success");
		this.fail = fail;
	}
	
	public MultipleTargetsCommandResult(Success success, Fail fail) {
		this.success = success;
		this.fail = fail;
	}
	
	public int trySend(CommandSourceStack source, boolean logToServerConsole,
			Collection<? extends Entity> targets, int successful, 
			Object... successTlArgs) throws CommandSyntaxException {
		if (successful <= 0) {
			throw fail.create(targets);
		}
		return success.send(source, logToServerConsole, targets, successful, successTlArgs);
	}

	public static class Fail {
		public final DynamicCommandExceptionType ERROR_SINGLE;
		public final DynamicCommandExceptionType ERROR_MULTIPLE;

		public Fail(String key) {
			ERROR_SINGLE = new DynamicCommandExceptionType(
					player -> Component.translatable(key + ".single", player));
			ERROR_MULTIPLE = new DynamicCommandExceptionType(
					playerCount -> Component.translatable(key + ".multiple", playerCount));
		}
		
		public CommandSyntaxException create(Collection<? extends Entity> targets) {
			return targets.size() == 1 ? ERROR_SINGLE.create(targets.iterator().next().getDisplayName()) : ERROR_MULTIPLE.create(targets.size());
		}
	}
	
	public static class Success {
		public final String tlKeySingle;
		public final String tlKeyMultiple;

		public Success(String key) {
			this.tlKeySingle = key + ".single";
			this.tlKeyMultiple = key + ".multiple";
		}
		
		public Component create(Collection<? extends Entity> targets, int successful, 
				Object... tlArgs) {
			Object[] args = new Object[tlArgs.length + 1];
			System.arraycopy(tlArgs, 0, args, 0, tlArgs.length);
			if (targets.size() > 1) {
				args[args.length - 1] = successful;
				return Component.translatable(tlKeyMultiple, args);
			}
			else {
				args[args.length - 1] = targets.iterator().next().getDisplayName();
				return Component.translatable(tlKeySingle, args);
			}
		}
		
		public int send(CommandSourceStack source, boolean logToServerConsole,
				Collection<? extends Entity> targets, int successful, 
				Object... tlArgs) {
			source.sendSuccess(() -> create(targets, successful, tlArgs), logToServerConsole);
			return successful;
		}
	}
}

package com.github.standobyte.jojo.command;

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
		return trySend(source, logToServerConsole, targets, successful, successTlArgs, successTlArgs);
	}
	
	public int trySend(CommandSourceStack source, boolean logToServerConsole,
			Collection<? extends Entity> targets, int successful, 
			Object[] successTlArgsSingle, Object[] successTlArgsMultiple) throws CommandSyntaxException {
		if (successful <= 0) {
			throw fail.create(targets);
		}
		return success.send(source, logToServerConsole, targets, successful, successTlArgsSingle, successTlArgsMultiple);
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
		
		public Component create(Collection<? extends Entity> targets, int successfulCount, 
				Object[] tlArgsSingle, Object[] tlArgsMultiple) {
			String tlKey;
			Object[] argsOriginal;
			Object lastArg;
			
			if (targets.size() > 1) {
				tlKey = tlKeyMultiple;
				argsOriginal = tlArgsMultiple;
				lastArg = successfulCount;
			}
			else {
				tlKey = tlKeySingle;
				argsOriginal = tlArgsSingle;
				lastArg = targets.iterator().next().getDisplayName();
			}
			
			Object[] args = new Object[argsOriginal.length + 1];
			System.arraycopy(argsOriginal, 0, args, 0, argsOriginal.length);
			args[args.length - 1] = lastArg;
			return Component.translatable(tlKey, args);
		}
		
		public int send(CommandSourceStack source, boolean logToServerConsole,
				Collection<? extends Entity> targets, int successfulCount, 
				Object[] tlArgsSingle, Object[] tlArgsMultiple) {
			source.sendSuccess(() -> create(targets, successfulCount, tlArgsSingle, tlArgsMultiple), logToServerConsole);
			return successfulCount;
		}
	}
}

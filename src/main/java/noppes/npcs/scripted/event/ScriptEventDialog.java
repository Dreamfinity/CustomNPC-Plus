package noppes.npcs.scripted.event;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.Dialog;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.interfaces.IPlayer;

public class ScriptEventDialog extends ScriptEvent{
	public IPlayer player;
	public int dialog;
	public int option;
	public Dialog dialogObj;

	public ScriptEventDialog(EntityPlayer player, int dialog, int option, Dialog dialogObj){
		this.player = (IPlayer)NpcAPI.Instance().getIEntity(player);
		this.dialog = dialog;
		this.option = option;
		this.dialogObj = dialogObj;
	}

	public IPlayer getPlayer() {
		return player;
	}

	public Dialog getDialog() {
		return dialogObj;
	}

	public int getDialogId() {
		return dialog;
	}

	public int getOptionId() {
		return option;
	}

	/**
	 * @deprecated
	 */
	public boolean isClosing(){
		return true;
	}
}

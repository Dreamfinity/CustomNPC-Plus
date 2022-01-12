//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.wrapper;

import java.io.File;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.eventhandler.EventBus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.*;
import noppes.npcs.scripted.*;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.ScriptContainer;
import noppes.npcs.scripted.entity.*;
import noppes.npcs.scripted.gui.ScriptGui;
import noppes.npcs.scripted.handler.*;
import noppes.npcs.scripted.interfaces.*;
import noppes.npcs.util.JsonException;
import noppes.npcs.util.LRUHashMap;
import noppes.npcs.util.NBTJsonUtil;

public class WrapperNpcAPI extends NpcAPI {
    private static final Map<Integer, ScriptWorld> worldCache = new LRUHashMap(10);
    public static final EventBus EVENT_BUS = new EventBus();
    private static NpcAPI instance = null;

    public WrapperNpcAPI() {
    }

    public static void clearCache() {
        worldCache.clear();
    }

    public IFactionHandler getFactions() {
        this.checkWorld();
        return FactionController.getInstance();
    }

    public IRecipeHandler getRecipes() {
        this.checkWorld();
        return RecipeController.instance;
    }

    public IQuestHandler getQuests() {
        this.checkWorld();
        return QuestController.instance;
    }

    public IDialogHandler getDialogs() {
        return DialogController.instance;
    }

    public ICloneHandler getClones() {
        return ServerCloneController.Instance;
    }

    public IEntity getIEntity(Entity entity) {
        if(entity == null)
            return null;
        if(entity instanceof EntityNPCInterface)
            return ((EntityNPCInterface)entity).wrappedNPC;
        else{
            ScriptEntityData data = (ScriptEntityData) entity.getExtendedProperties("ScriptedObject");
            if(data != null)
                return data.base;
            if(entity instanceof EntityPlayerMP)
                data = new ScriptEntityData(new ScriptPlayer((EntityPlayerMP) entity));
            else if(PixelmonHelper.isPixelmon(entity))
                return new ScriptPixelmon((EntityTameable) entity);
            else if(entity instanceof EntityAnimal)
                data = new ScriptEntityData(new ScriptAnimal((EntityAnimal) entity));
            else if(entity instanceof EntityMob)
                data = new ScriptEntityData(new ScriptMonster((EntityMob) entity));
            else if(entity instanceof EntityLiving)
                data = new ScriptEntityData(new ScriptLiving((EntityLiving) entity));
            else if(entity instanceof EntityLivingBase)
                data = new ScriptEntityData(new ScriptLivingBase((EntityLivingBase)entity));
            else
                data = new ScriptEntityData(new ScriptEntity(entity));
            entity.registerExtendedProperties("ScriptedObject", data);
            return data.base;
        }
    }

    public IBlock getIBlock(World world, BlockPos pos) {
        return new ScriptBlock(world, world.getBlock(pos.getX(),pos.getY(),pos.getZ()), pos);
    }

    public IBlock getIBlock(World world, int x, int y, int z) {
        return new ScriptBlock(world, world.getBlock(x, y, z), new BlockPos(x,y,z));
    }

    public INbt getINbt(NBTTagCompound compound) {
        return compound == null?new ScriptNbt(new NBTTagCompound()):new ScriptNbt(compound);
    }

    public INbt stringToNbt(String str) {
        if (str != null && !str.isEmpty()) {
            try {
                return this.getINbt(NBTJsonUtil.Convert(str));
            } catch (JsonException var3) {
                throw new CustomNPCsException(var3, "Failed converting " + str, new Object[0]);
            }
        } else {
            throw new CustomNPCsException("Cant cast empty string to nbt", new Object[0]);
        }
    }

    public ICustomNpc createNPC(World world) {
        if (world.isRemote) {
            return null;
        } else {
            EntityCustomNpc npc = new EntityCustomNpc(world);
            return npc.wrappedNPC;
        }
    }

    public ICustomNpc spawnNPC(World world, int x, int y, int z) {
        if (world.isRemote) {
            return null;
        } else {
            EntityCustomNpc npc = new EntityCustomNpc(world);
            npc.setPositionAndRotation((double)x + 0.5D, (double)y, (double)z + 0.5D, 0.0F, 0.0F);
            npc.setHealth(npc.getMaxHealth());
            world.spawnEntityInWorld(npc);
            return npc.wrappedNPC;
        }
    }

    public static NpcAPI Instance() {
        if (instance == null) {
            instance = new WrapperNpcAPI();
        }

        return instance;
    }

    public EventBus events() {
        return EVENT_BUS;
    }

    public IItemStack getIItemStack(ItemStack itemstack) {
        return (IItemStack)(itemstack != null && itemstack.stackSize > 0 ? new ScriptItemStack(itemstack) : new ScriptItemStack(new ItemStack(Item.getItemById(0))));

    }

    public IWorld getIWorld(WorldServer world) {
        ScriptWorld w = (ScriptWorld)worldCache.get(world.provider.dimensionId);
        if (w != null) {
            w.world = world;
            return w;
        } else {
            worldCache.put(world.provider.dimensionId, w = ScriptWorld.createNew(world));
            return w;
        }
    }

    public IWorld getIWorld(int dimensionId) {
        WorldServer[] var2 = CustomNpcs.getServer().worldServers;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            WorldServer world = var2[var4];
            if (world.provider.dimensionId == dimensionId) {
                return this.getIWorld(world);
            }
        }

        throw new CustomNPCsException("Unknown dimension id: " + dimensionId, new Object[0]);
    }

    public IContainer getIContainer(IInventory inventory) {
        return new ScriptContainer(inventory);
    }

    public IContainer getIContainer(Container container) {
        return (IContainer)(container instanceof ContainerNpcInterface ? ContainerNpcInterface.getOrCreateIContainer((ContainerNpcInterface)container) : new ScriptContainer(container));
    }

    private void checkWorld() {
        if (CustomNpcs.getServer() == null || CustomNpcs.getServer().isServerStopped()) {
            throw new CustomNPCsException("No world is loaded right now", new Object[0]);
        }
    }

    public IWorld[] getIWorlds() {
        this.checkWorld();
        IWorld[] worlds = new IWorld[CustomNpcs.getServer().worldServers.length];

        for(int i = 0; i < CustomNpcs.getServer().worldServers.length; ++i) {
            worlds[i] = this.getIWorld(CustomNpcs.getServer().worldServers[i]);
        }

        return worlds;
    }

    public File getGlobalDir() {
        return CustomNpcs.Dir;
    }

    public File getWorldDir() {
        return CustomNpcs.getWorldSaveDirectory();
    }

    public IDamageSource getIDamageSource(DamageSource damagesource) {
        return new ScriptDamageSource(damagesource);
    }

    public IDamageSource getIDamageSource(IEntity entity) {
        return new ScriptDamageSource(new EntityDamageSource(entity.getTypeName(),entity.getMCEntity()));
    }

    public void executeCommand(IWorld world, String command) {
        //if(!world.getMCWorld().isRemote)
            NoppesUtilServer.runCommand(world.getMCWorld(), "API", command);
    }
    public String getRandomName(int dictionary, int gender) {
        return CustomNpcs.MARKOV_GENERATOR[dictionary].fetch(gender);
    }

    public ScriptPlayer[] getAllServerPlayers(){
        List<EntityPlayer> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        ScriptPlayer[] arr = new ScriptPlayer[list.size()];
        for(int i = 0; i < list.size(); i++){
            arr[i] = (ScriptPlayer) ScriptController.Instance.getScriptForEntity(list.get(i));
        }

        return arr;
    }

    public ScriptItemStack createItem(String id, int damage, int size){
        Item item = (Item)Item.itemRegistry.getObject(id);
        if(item == null)
            return null;
        return new ScriptItemStack(new ItemStack(item, size, damage));
    }

    public ScriptEntityParticle createEntityParticle(String directory){
        return new ScriptEntityParticle(directory);
    }

    public int getServerTime() {
        return MinecraftServer.getServer().getTickCounter();
    }

    public ICustomGui createCustomGui(int id, int width, int height, boolean pauseGame) {
        return new ScriptGui(id, width, height, pauseGame);
    }
}

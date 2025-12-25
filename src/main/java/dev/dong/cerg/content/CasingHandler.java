package dev.dong.cerg.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import dev.dong.cerg.util.S2E;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;
import static com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType.ANDESITE;
import static com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType.BRASS;

public class CasingHandler {

    public static int MAX_CHAIN = 64;

    protected static void chainEncase(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockState originState = level.getBlockState(pos);
        Block originBlock = originState.getBlock();
        if (originBlock instanceof BeltBlock) {
            encaseBelt(event);
            return;
        }

        ItemStack heldItemStack = event.getItemStack();
        Item heldItem = heldItemStack.getItem();
        Player player = event.getEntity();
        var ray = event.getHitVec();
        var hand = event.getHand();

        // TODO 获取相连方块 get connected block
        List<BlockPos> connected = List.of(pos.east(), pos.south(), pos.west(), pos.north());

        for (BlockPos workmatePos : connected) {
            BlockState workmateState = level.getBlockState(workmatePos);
            Block workmate = workmateState.getBlock();
            if (!(workmate instanceof EncasableBlock)) continue;

            for (Block tmp : EncasingRegistry.getVariants(workmate)) {
                if (!(tmp instanceof EncasedBlock encased)) continue;
                if (encased.getCasing().asItem() != heldItem) continue;
                encased.handleEncasing(workmateState, level, workmatePos, heldItemStack, player, hand, ray);
                break;
            }
        }// end for: connected

    }

    private static void encaseBelt(PlayerInteractEvent.RightClickBlock event) {
        BlockPos originPos = event.getPos();
        Player player = event.getEntity();
        Level world = event.getLevel();
        ItemStack heldItemStack = event.getItemStack();
        Item heldItem = heldItemStack.getItem();

        boolean isBrass = AllBlocks.BRASS_CASING.is(heldItem);
        if (!isBrass && !AllBlocks.ANDESITE_CASING.is(heldItem)) return;

        SoundType soundType = (isBrass ? AllBlocks.BRASS_CASING : AllBlocks.ANDESITE_CASING)
                .getDefaultState()
                .getSoundType(world, originPos, player);
        List<BlockPos> chain = BeltBlock.getBeltChain(world, BeltHelper.getControllerBE(world, originPos).getBlockPos());
        if (chain.isEmpty()) return;

        world.playSound(null, originPos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        for (BlockPos p : chain) {
            BlockState s = world.getBlockState(p);
            var b = (BeltBlock) s.getBlock();
            b.withBlockEntityDo(world, p, be -> be.setCasingType(isBrass ? BRASS : ANDESITE));
            b.updateCoverProperty(world, p, s);
        }
        event.setCanceled(true);
    }

    protected static void chainDecase(PlayerInteractEvent.RightClickBlock event) {
        BlockPos originPos = event.getPos();
        Player player = event.getEntity();
        Level world = event.getLevel();
        BlockState originState = world.getBlockState(originPos);
        Block originBlock = originState.getBlock();
        boolean isCrouching = player.isCrouching();

        // 传送带拆壳
        if (originBlock instanceof BeltBlock) {
            if (isCrouching) return;
            BeltBlockEntity beltCtrl = BeltHelper.getControllerBE(world, originPos);
            if (beltCtrl == null) return;

            List<BlockPos> chain = BeltBlock.getBeltChain(world, beltCtrl.getBlockPos());
            if (chain.isEmpty()) return;

            for (BlockPos p : chain) {
                BlockState s = world.getBlockState(p);
                var b = (BeltBlock) s.getBlock();
                b.withBlockEntityDo(world, p, be -> be.setCasingType(BeltBlockEntity.CasingType.NONE));
            }

            event.setCanceled(true);
            return;
        }

        if (!(originBlock instanceof RotatedPillarKineticBlock && originBlock instanceof EncasedBlock)) return;
        event.setCanceled(true);

        // 传动方块拆壳
        var context = new UseOnContext(player, event.getHand(), event.getHitVec());
        ((RotatedPillarKineticBlock) originBlock).onSneakWrenched(originState, context);

        var axis = originState.getValue(RotatedPillarKineticBlock.AXIS);
        S2E ofs = new S2E(originPos);
        S2E vec = switch (axis) {
            case X -> S2E.axisX();
            case Y -> S2E.axisY();
            case Z -> S2E.axisZ();
        };
        boolean sFlag = true, eFlag = true;
        int count = 1;

        // 沿轴遍历，无所谓传动轴or齿轮
        while ((sFlag || eFlag) && count < MAX_CHAIN) {
            ofs.expand(vec);
            if (sFlag) {
                if (tryDecase(event, ofs.getStart(), axis)) count++;
                else sFlag = false;
            }
            if (eFlag) {
                if (tryDecase(event, ofs.getEnd(), axis)) count++;
                else eFlag = false;
            }
        }
    }

    private static boolean tryDecase(PlayerInteractEvent.RightClickBlock event, BlockPos p, Direction.Axis axis) {
        BlockState s = event.getLevel().getBlockState(p);
        if (s.isAir()) return false;
        Block b = s.getBlock();

        // flag = 轴传动 && 轴同向
        if (!(b instanceof RotatedPillarKineticBlock w) || s.getValue(AXIS) != axis) return false;

        var context = new UseOnContext(event.getEntity(), event.getHand(), event.getHitVec().withPosition(p));
        // 只处理已包机壳的方块
        if (b instanceof EncasedBlock) w.onSneakWrenched(s, context);
        return true;
    }

}

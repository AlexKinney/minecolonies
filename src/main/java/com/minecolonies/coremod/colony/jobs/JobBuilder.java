package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.Model;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingBuilder;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrderBuild;
import com.minecolonies.coremod.entity.ai.citizen.builder.EntityAIStructureBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The job of the builder.
 */
public class JobBuilder extends AbstractJobStructure
{
    /**
     * Tag to store the workOrder id.
     */
    private static final String TAG_WORK_ORDER = "workorder";

    /**
     * The id of the current workOrder.
     */
    private int workOrderId;

    /**
     * Instantiates builder job.
     *
     * @param entity citizen.
     */
    public JobBuilder(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_WORK_ORDER))
        {
            workOrderId = compound.getInteger(TAG_WORK_ORDER);
        }
    }

    @NotNull
    @Override
    public Model getModel()
    {
        return Model.BUILDER;
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (workOrderId != 0)
        {
            compound.setInteger(TAG_WORK_ORDER, workOrderId);
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Builder";
    }

    @NotNull
    @Override
    public AbstractAISkeleton<JobBuilder> generateAI()
    {
        return new EntityAIStructureBuilder(this);
    }

    /**
     * Get the Work Order ID for this Job.
     *
     * @return UUID of the Work Order claimed by this Job, or null
     */
    public int getWorkOrderId()
    {
        return workOrderId;
    }

    /**
     * Does this job have a Work Order it has claimed?
     *
     * @return true if there is a Work Order claimed by this Job
     */
    public boolean hasWorkOrder()
    {
        return workOrderId != 0;
    }

    /**
     * Returns the work interval of the worker.
     *
     * @return work interval
     */
    public int getWorkInterval()
    {
        //TODO
        //Constants.BUILDERWORKINTERFALL - this.getLevel();
        return 1;
    }

    /**
     * Do final completion when the Job's current work is complete.
     */
    public void complete()
    {
        final BlockPos buildingLocation = this.getWorkOrder().getBuildingLocation();
        final AbstractBuilding building = (AbstractBuilding) this.getCitizen().getColony().getBuilding(buildingLocation);

        if (building != null)
        {
            this.getCitizen().getColony().onBuildingUpgradeComplete(building, this.getWorkOrder().getUpgradeLevel());
        }

        getCitizen().getColony().getWorkManager().removeWorkOrder(workOrderId);
        setWorkOrder(null);
        setStructure(null);
        this.getColony().incrementStatistic("huts");
    }

    /**
     * Get the Work Order for the Job.
     * Warning: AbstractWorkOrder is not cached
     *
     * @return AbstractWorkOrderBuild for the Build
     */
    public AbstractWorkOrderBuild getWorkOrder()
    {
        return getColony().getWorkManager().getWorkOrder(workOrderId, AbstractWorkOrderBuild.class);
    }

    /**
     * Reset the needed items list.
     */
    private void resetNeededItems()
    {
        final IBuilding workerBuilding = this.getCitizen().getWorkBuilding();
        if (workerBuilding instanceof BuildingBuilder)
        {
            ((BuildingBuilder) workerBuilding).resetNeededResources();
        }
    }

    /**
     * Set a Work Order for this Job.
     *
     * @param order Work Order to associate with this job, or null
     */
    public void setWorkOrder(@Nullable final AbstractWorkOrderBuild order)
    {
        if (order == null)
        {
            workOrderId = 0;
            resetNeededItems();
        }
        else
        {
            workOrderId = order.getID();
        }
    }
}

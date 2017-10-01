package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.Model;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic job information.
 * <p>
 * Suppressing Sonar Rule squid:S2390
 * This rule does "Classes should not access static members of their own subclasses during initialization"
 * But in this case the rule does not apply because
 * We are only mapping classes and that is reasonable
 */
@SuppressWarnings("squid:S2390")
public abstract class AbstractJob implements IJob
{
    private static final String TAG_TYPE         = "type";
    private static final String TAG_ITEMS_NEEDED = "itemsNeeded";

    private static final String MAPPING_PLACEHOLDER = "Placeholder";
    private static final String MAPPING_BUILDER     = "Builder";
    private static final String MAPPING_DELIVERY    = "Deliveryman";
    private static final String MAPPING_MINER       = "Miner";
    private static final String MAPPING_LUMBERJACK  = "Lumberjack";
    private static final String MAPPING_FARMER      = "Farmer";
    private static final String MAPPING_FISHERMAN   = "Fisherman";
    private static final String MAPPING_TOWER_GUARD = "GuardTower";

    /**
     * The priority assigned with every main AI job.
     */
    private static final int TASK_PRIORITY = 3;

    //  Job and View Class Mapping.
    @NotNull
    private static final Map<String, Class<? extends IJob>> nameToClassMap = new HashMap<>();
    @NotNull
    private static final Map<Class<? extends IJob>, String> classToNameMap = new HashMap<>();
    //fix for the annotation
    static
    {
        addMapping(MAPPING_PLACEHOLDER, JobPlaceholder.class);
        addMapping(MAPPING_BUILDER, JobBuilder.class);
        addMapping(MAPPING_DELIVERY, JobDeliveryman.class);
        addMapping(MAPPING_MINER, JobMiner.class);
        addMapping(MAPPING_LUMBERJACK, JobLumberjack.class);
        addMapping(MAPPING_FARMER, JobFarmer.class);
        addMapping(MAPPING_FISHERMAN, JobFisherman.class);
        addMapping(MAPPING_TOWER_GUARD, JobGuard.class);
    }

    private final ICitizenData citizen;
    private String nameTag = "";

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public AbstractJob(final ICitizenData entity)
    {
        citizen = entity;
    }

    /**
     * Add a given Job mapping.
     *
     * @param name     name of job class.
     * @param jobClass class of job.
     */
    private static void addMapping(final String name, @NotNull final Class<? extends AbstractJob> jobClass)
    {
        if (nameToClassMap.containsKey(name))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding Job class mapping");
        }
        try
        {
            if (jobClass.getDeclaredConstructor(CitizenData.class) != null)
            {
                nameToClassMap.put(name, jobClass);
                classToNameMap.put(jobClass, name);
            }
        }
        catch (final NoSuchMethodException exception)
        {
            throw new IllegalArgumentException("Missing constructor for type '" + name + "' when adding Job class mapping", exception);
        }
    }

    /**
     * Create a Job from saved NBTTagCompound data.
     *
     * @param citizen  The citizen that owns the Job.
     * @param compound The NBTTagCompound containing the saved Job data.
     * @return New Job created from the data, or null.
     */
    @Nullable
    public static IJob createFromNBT(final ICitizenData citizen, @NotNull final NBTTagCompound compound)
    {
        @Nullable IJob job = null;
        @Nullable Class<? extends IJob> oclass = null;

        try
        {
            oclass = nameToClassMap.get(compound.getString(TAG_TYPE));

            if (oclass != null)
            {
                final Constructor<?> constructor = oclass.getDeclaredConstructor(ICitizenData.class);
                job = (AbstractJob) constructor.newInstance(citizen);
            }
        }
        catch (@NotNull NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e)
        {
            Log.getLogger().trace(e);
        }

        if (job != null)
        {
            try
            {
                job.readFromNBT(compound);
            }
            catch (final RuntimeException ex)
            {
                Log.getLogger().error(String.format("A Job %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                  compound.getString(TAG_TYPE), oclass.getName()), ex);
                job = null;
            }
        }
        else
        {
            Log.getLogger().warn(String.format("Unknown Job type '%s' or missing constructor of proper format.", compound.getString(TAG_TYPE)));
        }

        return job;
    }

    /**
     * Restore the Job from an NBTTagCompound.
     *
     * @param compound NBTTagCompound containing saved Job data.
     */
    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        //NOOP; Requests are stored on the building.
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @Override
    public Model getModel()
    {
        return Model.CITIZEN;
    }

    /**
     * Get the CitizenData that this Job belongs to.
     *
     * @return CitizenData that owns this Job.
     */
    @Override
    public ICitizenData getCitizen()
    {
        return citizen;
    }

    /**
     * Get the Colony that this Job is associated with (shortcut for getCitizen().getColony()).
     *
     * @return {@link Colony} of the citizen.
     */
    @Override
    public IColony getColony()
    {
        return citizen.getColony();
    }

    /**
     * Save the Job to an NBTTagCompound.
     *
     * @param compound NBTTagCompound to save the Job to.
     */
    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        final String s = classToNameMap.get(this.getClass());

        if (s == null)
        {
            throw new IllegalStateException(this.getClass() + " is missing a mapping! This is a bug!");
        }

        compound.setString(TAG_TYPE, s);
    }

    /**
     * Does the Job have _all_ the needed items.
     *
     * @return true if the Job has no needed items.
     */
    @Override
    public boolean isMissingNeededItem()
    {
        return citizen.getWorkBuilding().hasWorkerOpenRequests(citizen);
    }

    /**
     * Method used to create a request in the workers building.
     *
     * @param request   The request to create.
     * @param <Request> The type of request.
     */
    @Override
    public <Request> void createRequest(@NotNull final Request request)
    {
        citizen.getWorkBuilding().createRequest(citizen, request);
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     *
     * @param tasks EntityAITasks list to add tasks to.
     */
    @Override
    public void addTasks(@NotNull final EntityAITasks tasks)
    {
        final AbstractAISkeleton<? extends IJob> aiTask = generateAI();
        if (aiTask != null)
        {
            tasks.addTask(TASK_PRIORITY, aiTask);
        }
    }

    /**
     * This method can be used to display the current status.
     * That a citizen is having.
     *
     * @return Small string to display info in name tag
     */
    @Override
    public String getNameTagDescription()
    {
        return this.nameTag;
    }

    /**
     * Used by the AI skeleton to change a citizens name.
     * Mostly used to update debugging information.
     *
     * @param nameTag The name tag to display.
     */
    @Override
    public final void setNameTag(final String nameTag)
    {
        this.nameTag = nameTag;
    }

    /**
     * Override this to let the worker return a bedTimeSound.
     *
     * @return soundEvent to be played.
     */
    @Override
    public SoundEvent getBedTimeSound()
    {
        return null;
    }

    /**
     * Override this to let the worker return a badWeatherSound.
     *
     * @return soundEvent to be played.
     */
    @Override
    public SoundEvent getBadWeatherSound()
    {
        return null;
    }
}

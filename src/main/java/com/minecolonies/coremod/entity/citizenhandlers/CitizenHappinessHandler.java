package com.minecolonies.coremod.entity.citizenhandlers;

import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.FieldDataModifier;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.util.ChatSpamFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

/**
 * Handler for the citizens happiness.
 * This keeps all the modifiers related to the citizen.
 *
 * @author kevin
 *
 */
public class CitizenHappinessHandler
{

    @NotNull
    protected final ChatSpamFilter chatSpamFilter;

    /**
     * Tag names for NBT fields
     */
    private static final String TAG_NAME = "happiness";
    private static final String TAG_BASE = "base";
    private static final String TAG_FOOD = "foodModifier";
    private static final String TAG_DAMAGE = "damageModifier";
    private static final String TAG_HOUSE = "houseModifier";
    private static final String TAG_NUMBER_OF_DAYS_HOUSE = "numberOfDaysWithoutHouse";
    private static final String TAG_JOB = "jobModifier";
    private static final String TAG_NUMBER_OF_DAYS_JOB = "numberOfDaysWithoutJob";
    private static final String TAG_FIELDS = "fields";
    private static final String TAG_HAS_NO_FIELDS = "hasNoFields";
    private static final String TAG_FIELD_DAYS_INACTIVE = "daysinactive";
    private static final String TAG_FIELD_ID = "id";
    private static final String TAG_FIELD_CAN_FARM = "canfarm";
    private static final String TAG_NO_TOOLS = "noTools";
    private static final String TAG_NO_TOOLS_NUMBER_DAYS = "numberOfDaysNoTools";
    private static final String TAG_NO_TOOLS_TOOL_TYPE = "toolType";

    /**
     * constants for house modifier.
     */
    public static final int MAX_DAYS_WITHOUT_HOUSE = 30;
    public static final int MAX_HOUSE_PENALTY = 5;
    public static final double HOUSE_MODIFIER_POSITIVE = 0.5;
    public static final int COMPLAIN_DAYS_WITHOUT_HOUSE = 7;
    public static final int DEMANDS_DAYS_WITHOUT_HOUSE = 14;

    /**
     * constants for job modifier.
     */
    public static final int MAX_DAYS_WITHOUT_JOB = 30;
    public static final int COMPLAIN_DAYS_WITHOUT_JOB = 7;
    public static final int DEMANDS_DAYS_WITHOUT_JOB = 14;
    public static final int MAX_JOB_PENALTY = 5;
    public static final double JOB_MODIFIER_POSITIVE = 0.5;

    /**
     * constants for food modifier.
     */
    public static final int FOOD_MODIFIER_MAX = -2;
    public static final int FOOD_MODIFIER_MIN = -1;
    public static final double FOOD_MODIFIER_POSITIVE = 0.5;

    /**
     * constants for field modifiers
     */
    public static final int FIELD_MAX_DAYS_MODIFIER = 30;
    public static final double FIELD_MODIFIER_MAX = -0.75;
    public static final double FIELD_MODIFIER_MIN = -0.15;
    public static final double FIELD_MODIFIER_POSITIVE = 0.2;
    public static final double NO_FIELD_MODIFIER = -3.0;
    public static final int NO_FIELDS_COMPLAINS_DAYS = 7;

    /**
     * constants for damage modifiers
     */
    public static final int DAMAGE_MODIFIER_MAX = -2;
    public static final int DAMAGE_MODIFIER_MID = -1;
    public static final double DAMAGE_MODIFIER_MIN = -0.5;
    public static final double DAMAGE_LOWEST_POINT = 0.25d;
    public static final double DAMAGE_MEDIUM_POINT = 0.50d;
    public static final double DAMAGE_HIGHEST_POINT = 0.75d;
    
    /**
     * constants for happiness min/max and start happines values.
     */
    public static final int MAX_HAPPINESS = 10;
    public static final int MIN_HAPPINESS = 1;
    public static final int BASE_HAPPINESS = 8;

    /**
     * constants for no tools.
     */
    public static final double NO_TOOL_MODIFIER = -3.0;
    public static final int NO_TOOLS_MODIFIER = 3;
    public static final int NO_TOOLS_COMPLAINS_DAYS = 7;
    public static final int NO_TOOLS_DEMANDS_DAYS = 14;
    public static final int NO_TOOLS_MAX_DAYS_MODIFIER = 30;

    /**
     * The citizen assigned to this manager.
     */
    private final CitizenData citizen;

    /**
     * holds the base happiness value.
     */
    private double baseHappiness;

    /**
     * holds the modifier for food.
     */
    private double foodModifier;

    /**
     * holds the modifier for damage.
     */
    private double damageModifier;

    /**
     * holds the modifier for house.
     */
    private double houseModifier;

    /**
     * holds the numbers of days without a house.
     */
    private int numberOfDaysWithoutHouse;

    /**
     * holds the modifier for job.
     */
    private double jobModifier;

    /**
     * holds the numbers of days without a job.
     */
    private int numberOfDaysWithoutJob;

    /**
     * holds the modifier for farms that the farmer can't farm.
     */
    private double farmerModifier;

    /**
     * holds an indicator of the farmer has no fields to farm
     */
    private boolean hasNoFields;

    /**
     * holds an array of all the fields the farmer has assigned to them.
     */
    private Map<BlockPos, FieldDataModifier> fieldModifier = new HashMap<BlockPos, FieldDataModifier>();

    /**
     * holds an indicator citizen needing a tool
     */
    private final Map<IToolType, Integer> needsTool = new HashMap<IToolType, Integer>();

    /**
     * holds the modifier for citizen not having a tool.
     */
    private double noToolModifier;

    /**
     * Constructor for the experience handler.
     *
     * @param citizen
     *            the citizen owning the handler.
     */
    public CitizenHappinessHandler(final CitizenData citizen)
    {
        this.citizen = citizen;
        baseHappiness = BASE_HAPPINESS;
        foodModifier = 0;
        damageModifier = 0;
        houseModifier = 0;
        numberOfDaysWithoutHouse = 0;
        jobModifier = 0;
        numberOfDaysWithoutJob = 0;
        farmerModifier = 0;
        hasNoFields = false;
        needsTool.clear();
        chatSpamFilter = new ChatSpamFilter(citizen);

    }

    /**
     * This function applies eating adjust to the base hapiness for
     * the citizen.
     *
     * @param eatFood
     *            true or false indicate citizen was unable to eat
     */
    public void setFoodModifier(final boolean eatFood)
    {
        if (!eatFood)
        {
            if (citizen.getSaturation() < CitizenConstants.LOW_SATURATION)
            {
                foodModifier = FOOD_MODIFIER_MAX;
            }
            else
            {
                foodModifier = FOOD_MODIFIER_MIN;
            }
        }
        else
        {
            foodModifier = 0;
        }
    }

    /**
     * Called once a day to update the citizens daily happiness
     * modifiers.
     *
     * @param hasHouse
     *            boolean if the citizen is assigned to a house
     * @param hasJob
     *            boolean to indicate if citizen has a job
     */
    public void processDailyHappiness(final boolean hasHouse, final boolean hasJob)
    {
        if (!hasHouse)
        {
            numberOfDaysWithoutHouse++;
            if (numberOfDaysWithoutHouse > DEMANDS_DAYS_WITHOUT_HOUSE)
            {
                chatSpamFilter.talkWithoutSpam("entity.citizen.demandsHouse", citizen.getName());
            }
            else if (numberOfDaysWithoutHouse > COMPLAIN_DAYS_WITHOUT_HOUSE)
            {
                chatSpamFilter.talkWithoutSpam("entity.citizen.noHouse", citizen.getName());
            }
        }
        else
        {
            numberOfDaysWithoutHouse = 0;
        }
        setHomeModifier(hasHouse);

        if (!hasJob)
        {
            numberOfDaysWithoutJob++;
            if (numberOfDaysWithoutJob > DEMANDS_DAYS_WITHOUT_JOB)
            {
                chatSpamFilter.talkWithoutSpam("entity.citizen.demandsJob", citizen.getName());
            }
            else if (numberOfDaysWithoutJob > COMPLAIN_DAYS_WITHOUT_JOB)
            {
                chatSpamFilter.talkWithoutSpam("entity.citizen.noJob", citizen.getName());
            }
        }
        else
        {
            numberOfDaysWithoutJob = 0;
        }
        setJobModifier(hasJob);

        farmerModifier = 0;
        hasNoFields = true;
        for (final FieldDataModifier field : fieldModifier.values())
        {
            if (field.getCanFarm())
            {
                farmerModifier += FIELD_MODIFIER_POSITIVE;
                hasNoFields = false;
            }
            else
            {
                field.increaseInactiveDays();
                if (field.getInactiveDays() < NO_FIELDS_COMPLAINS_DAYS)
                {
                    farmerModifier += FIELD_MODIFIER_MIN;
                }
                else
                {
                    farmerModifier += ((double) ((double) field.getInactiveDays() / FIELD_MAX_DAYS_MODIFIER) * FIELD_MODIFIER_MAX) + FIELD_MODIFIER_MIN;
                }
            }
        }

        if (hasNoFields)
        {
            farmerModifier = NO_FIELD_MODIFIER;
        }

        noToolModifier = 0;
        for (final Map.Entry<IToolType, Integer> entry : needsTool.entrySet())
        {
            final int numDays = entry.getValue() + 1;
            final IToolType toolType = entry.getKey();
            needsTool.put(toolType, numDays);
            if (numDays > NO_TOOLS_DEMANDS_DAYS)
            {
                chatSpamFilter.talkWithoutSpam("entity.citizen.noTool", citizen.getName(), toolType.getDisplayName());
            }
            else if (numDays > NO_TOOLS_COMPLAINS_DAYS)
            {
                chatSpamFilter.talkWithoutSpam("entity.citizen.demandsTool", citizen.getName(), toolType.getDisplayName());
            }
            noToolModifier += ((double) ((double) numDays / NO_TOOLS_MAX_DAYS_MODIFIER) * NO_TOOLS_MODIFIER);
        }

        citizen.markDirty();

    }

    /**
     * this function applies adjust to happiness.
     * This would mean the citizen is full.
     */
    public void setSaturated()
    {
        foodModifier = FOOD_MODIFIER_POSITIVE;
    }

    /**
     * set the Damage modifier on the citizens happiness
     * depending on how hurt they are.
     */
    public void setDamageModifier()
    {
        final Optional<EntityCitizen> entityCitizen = citizen.getCitizenEntity();
        if (entityCitizen.isPresent())
        {
            final double health = entityCitizen.get().getHealth() / entityCitizen.get().getMaxHealth();
            if (health < DAMAGE_LOWEST_POINT)
            {
                damageModifier = DAMAGE_MODIFIER_MAX;
            }
            else if (health < DAMAGE_MEDIUM_POINT)
            {
                damageModifier = DAMAGE_MODIFIER_MID;
            }
            else if (health < DAMAGE_HIGHEST_POINT)
            {
                damageModifier = DAMAGE_MODIFIER_MIN;
            }
            citizen.markDirty();
        }
    }

    /**
     * Called to set if the farmer can farm a specific field.
     *
     * @param pos
     *            position of the scarecrow block
     * @param canFarm
     *            boolean to indicate if the field can be farmed
     */
    public void setNoFieldForFarmerModifier(final BlockPos pos, final boolean canFarm)
    {
        FieldDataModifier field = fieldModifier.get(pos);
        if (field == null)
        {
            field = new FieldDataModifier();
            fieldModifier.put(pos, field);
        }

        field.isCanFarm(canFarm);
    }

    /**
     * Indicates the farmer has not fields to farm.
     */
    public void setNoFieldsToFarm()
    {
        hasNoFields = true;
    }

    /**
     * Call this function to add a tool type that is needed by the citizen.
     * If citizen gets its tool, then passing in false will remove it from the needed tools.
     *
     * @param toolType Tooltype to indicate
     * @param needs indicate if the tool type is needed
     */
    public void setNeedsATool(@NotNull final IToolType toolType, final boolean needs)
    {
        if (needs)
        {
            if (!needsTool.containsKey(toolType))
            {
                needsTool.put(toolType, 0);
            }
        }
        else
        {
            needsTool.remove(toolType);
        }
    }

    /**
     * @param hasHouse
     *            indicate the citizen has an assigned house
     */
    public void setHomeModifier(final boolean hasHouse)
    {
        if (hasHouse)
        {
            houseModifier = HOUSE_MODIFIER_POSITIVE;
        }
        else
        {
            houseModifier = (MAX_HOUSE_PENALTY * ((double) numberOfDaysWithoutHouse / MAX_DAYS_WITHOUT_HOUSE)) * -1;
            citizen.markDirty();
        }
    }

    /**
     * Called to set if the citizen has a job or not.
     *
     * @param hasJob
     *            boolean to indicate if the citizen has a job
     */
    public void setJobModifier(final boolean hasJob)
    {
        if (hasJob)
        {
            jobModifier = JOB_MODIFIER_POSITIVE;
        }
        else
        {
            jobModifier = (MAX_JOB_PENALTY * ((double) numberOfDaysWithoutHouse / MAX_DAYS_WITHOUT_JOB)) * -1;
        }
    }

    /**
     * @return current citizens overall happiness
     */
    public double getHappiness()
    {
        double value = baseHappiness + foodModifier + damageModifier + houseModifier + jobModifier + farmerModifier + noToolModifier;
        if (value > MAX_HAPPINESS)
        {
            value = MAX_HAPPINESS;
        }
        else if (value < MIN_HAPPINESS)
        {
            value = MIN_HAPPINESS;
        }

        return value;
    }

    /**
     * @return current food modifier for happiness
     */
    public double getFoodModifier()
    {
        return foodModifier;
    }

    /**
     * @return current damage modifier for happiness
     */
    public double getDamageModifier()
    {
        return damageModifier;
    }

    /**
     * @return current house modifier for happiness
     */
    public double getHouseModifier()
    {
        return houseModifier;
    }

    /**
     * Store the level to nbt.
     *
     * @param compound
     *            compound to use.
     */
    public void writeToNBT(final NBTTagCompound compound)
    {
        @NotNull
        final NBTTagList tasksTagList = new NBTTagList();
        @NotNull
        final NBTTagCompound taskCompound = new NBTTagCompound();
        taskCompound.setDouble(TAG_BASE, baseHappiness);
        taskCompound.setDouble(TAG_FOOD, foodModifier);
        taskCompound.setDouble(TAG_DAMAGE, damageModifier);
        taskCompound.setDouble(TAG_HOUSE, houseModifier);
        taskCompound.setInteger(TAG_NUMBER_OF_DAYS_HOUSE, numberOfDaysWithoutHouse);

        taskCompound.setDouble(TAG_JOB, jobModifier);
        taskCompound.setInteger(TAG_NUMBER_OF_DAYS_JOB, numberOfDaysWithoutJob);
        taskCompound.setBoolean(TAG_HAS_NO_FIELDS, hasNoFields);

        @NotNull
        final NBTTagList fieldsTagList = new NBTTagList();
        for (final Map.Entry<BlockPos, FieldDataModifier> entry : fieldModifier.entrySet())
        {
            final BlockPos pos = entry.getKey();
            final FieldDataModifier field = entry.getValue();
            @NotNull
            final NBTTagCompound fieldCompound = new NBTTagCompound();
            fieldCompound.setInteger(TAG_FIELD_DAYS_INACTIVE, field.getInactiveDays());
            fieldCompound.setBoolean(TAG_FIELD_CAN_FARM, field.getCanFarm());

            @NotNull
            final NBTTagList containerTagList = new NBTTagList();
            containerTagList.appendTag(NBTUtil.createPosTag(pos));
            fieldCompound.setTag(TAG_FIELD_ID, containerTagList);

            fieldsTagList.appendTag(fieldCompound);
        }
        taskCompound.setTag(TAG_FIELDS, fieldsTagList);

        @NotNull
        final NBTTagList noToolsTagList = new NBTTagList();
        for (final Map.Entry<IToolType, Integer> entry : needsTool.entrySet())
        {
            final IToolType toolType = entry.getKey();
            final int numDays = entry.getValue();
            @NotNull
            final NBTTagCompound noToolsCompound = new NBTTagCompound();
            noToolsCompound.setInteger(TAG_NO_TOOLS_NUMBER_DAYS, numDays);
            noToolsCompound.setString(TAG_NO_TOOLS_TOOL_TYPE, toolType.getName());

            noToolsTagList.appendTag(noToolsCompound);
        }
        taskCompound.setTag(TAG_FIELDS, fieldsTagList);

        tasksTagList.appendTag(taskCompound);

        compound.setTag(TAG_NAME, tasksTagList);
    }

    /**
     * Reads in Happiness data from the NBT file.
     *
     * @param compound
     *            pointer to NBT fields
     */
    public void readFromNBT(final NBTTagCompound compound)
    {
        final NBTTagList tagListCompound = compound.getTagList(TAG_NAME, Constants.NBT.TAG_COMPOUND);
        final NBTTagCompound tagCompound = tagListCompound.getCompoundTagAt(0);
        baseHappiness = tagCompound.getDouble(TAG_BASE);
        foodModifier = tagCompound.getDouble(TAG_FOOD);
        damageModifier = tagCompound.getDouble(TAG_DAMAGE);
        houseModifier = tagCompound.getDouble(TAG_HOUSE);
        numberOfDaysWithoutHouse = tagCompound.getInteger(TAG_NUMBER_OF_DAYS_HOUSE);

        jobModifier = tagCompound.getDouble(TAG_JOB);
        numberOfDaysWithoutJob = tagCompound.getInteger(TAG_NUMBER_OF_DAYS_JOB);
        hasNoFields = tagCompound.getBoolean(TAG_HAS_NO_FIELDS);

        final NBTTagList fieldTagList = tagCompound.getTagList(TAG_FIELDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fieldTagList.tagCount(); ++i)
        {
            final FieldDataModifier field = new FieldDataModifier();
            final NBTTagCompound containerCompound = fieldTagList.getCompoundTagAt(i);
            field.setInactiveDays(containerCompound.getInteger(TAG_FIELD_DAYS_INACTIVE));
            field.isCanFarm(containerCompound.getBoolean(TAG_FIELD_CAN_FARM));

            final NBTTagList blockPosTagList = containerCompound.getTagList(TAG_FIELD_ID, Constants.NBT.TAG_COMPOUND);
            final NBTTagCompound blockPoCompound = blockPosTagList.getCompoundTagAt(0);
            final BlockPos pos = NBTUtil.getPosFromTag(blockPoCompound);
            fieldModifier.put(pos, field);
        }

        final NBTTagList noToolsTagList = tagCompound.getTagList(TAG_NO_TOOLS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < noToolsTagList.tagCount(); ++i)
        {
            final NBTTagCompound containerCompound = noToolsTagList.getCompoundTagAt(i);
            final int numDays = containerCompound.getInteger(TAG_NO_TOOLS_NUMBER_DAYS);
            final IToolType toolType = ToolType.getToolType(containerCompound.getString(TAG_NO_TOOLS_TOOL_TYPE));
            needsTool.put(toolType, numDays);
        }

        if (baseHappiness == 0)
        {
            baseHappiness = BASE_HAPPINESS;
        }
    }

}

/**
 *  Copyright (C) 2002-2012   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.freecolandroid.xml.stream.XMLStreamConstants;
import org.freecolandroid.xml.stream.XMLStreamException;
import org.freecolandroid.xml.stream.XMLStreamReader;
import org.freecolandroid.xml.stream.XMLStreamWriter;

import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.common.model.Map.Position;
import net.sf.freecol.common.model.NationOptions.NationState;
import net.sf.freecol.common.model.Region.RegionType;

import org.w3c.dom.Element;


/**
 * Represents a player. The player can be either a human player or an
 * AI-player.
 *
 * In addition to storing the name, nation e.t.c. of the player, it also stores
 * various defaults for the player. One example of this is the
 * {@link #getEntryLocation entry location}.
 */
public class Player extends FreeColGameObject implements Nameable {

    private static final Logger logger = Logger.getLogger(Player.class.getName());

    // A magic constant to denote that a players gold is not tracked.
    public static final int GOLD_NOT_ACCOUNTED = Integer.MIN_VALUE;

    public static final int SCORE_SETTLEMENT_DESTROYED = -40;

    public static final String ASSIGN_SETTLEMENT_NAME = "";

    /**
     * The XML tag name for the set of founding fathers.
     */
    private static final String FOUNDING_FATHER_TAG = "foundingFathers";

    /**
     * The XML tag name for the set of offered founding fathers.
     */
    private static final String OFFERED_FATHER_TAG = "offeredFathers";

    /**
     * The XML tag name for the stance array.
     */
    private static final String STANCE_TAG = "stance";

    /**
     * The XML tag name for the tension array.
     */
    private static final String TENSION_TAG = "tension";

    /**
     * The name of the unknown enemy.
     */
    public static final String UNKNOWN_ENEMY = "unknown enemy";

    /**
     * Constants for describing the stance towards a player.
     */
    public static enum Stance {
        // Valid transitions:
        //
        //   [FROM] \  [TO]  U  A  P  C  W        Reasons
        //   ----------------------------------   a = attack
        //   UNCONTACTED  |  -  x  c  x  i    |   c = contact
        //   ALLIANCE     |  x  -  d  x  adit |   d = act of diplomacy
        //   PEACE        |  x  d  -  x  adit |   i = incitement/rebellion
        //   CEASE_FIRE   |  x  d  t  -  adit |   t = change of tension
        //   WAR          |  x  d  ds dt -    |   s = surrender
        //   ----------------------------------   x = invalid
        //
        UNCONTACTED,
        ALLIANCE,
        PEACE,
        CEASE_FIRE,
        WAR;


        // Helpers to enforce valid transitions
        private void badStance() throws IllegalStateException {
            throw new IllegalStateException("Bogus stance");
        }
        private void badTransition(Stance newStance)
            throws IllegalStateException {
            throw new IllegalStateException("Bad transition: " + toString()
                                            + " -> " + newStance.toString());
        }

        /**
         * Check whether tension has changed enough to merit a stance
         * change.  Do not simply check for crossing tension
         * thresholds, add in Tension.DELTA to provide a bit of
         * hysteresis to dampen ringing.
         *
         * @param tension The <code>Tension</code> to check.
         * @return The <code>Stance</code> appropriate to the tension level.
         */
        public Stance getStanceFromTension(Tension tension) {
            int value = tension.getValue();
            switch (this) {
            case WAR: // Cease fire if tension decreases
                if (value <= Tension.Level.CONTENT.getLimit()-Tension.DELTA) {
                    return Stance.CEASE_FIRE;
                }
                break;
            case CEASE_FIRE: // Peace if tension decreases
                if (value <= Tension.Level.HAPPY.getLimit()-Tension.DELTA) {
                    return Stance.PEACE;
                }
                // Fall through
            case ALLIANCE: case PEACE: // War if tension increases
                if (value > Tension.Level.HATEFUL.getLimit()+Tension.DELTA) {
                    return Stance.WAR;
                }
                break;
            case UNCONTACTED:
                break;
            default:
                this.badStance();
            }
            return this;
        }

        /**
         * A stance change is about to happen.  Get the appropriate tension
         * modifier.
         *
         * @param newStance The new <code>Stance</code>.
         * @return A modifier to the current tension.
         */
        public int getTensionModifier(Stance newStance)
            throws IllegalStateException {
            switch (newStance) {
            case UNCONTACTED:     badTransition(newStance);
            case ALLIANCE:
                switch (this) {
                case UNCONTACTED: badTransition(newStance);
                case ALLIANCE:    return 0;
                case PEACE:       return Tension.ALLIANCE_MODIFIER;
                case CEASE_FIRE:  return Tension.ALLIANCE_MODIFIER + Tension.PEACE_TREATY_MODIFIER;
                case WAR:         return Tension.ALLIANCE_MODIFIER + Tension.CEASE_FIRE_MODIFIER + Tension.PEACE_TREATY_MODIFIER;
                default:          this.badStance();
                }
            case PEACE:
                switch (this) {
                case UNCONTACTED: return Tension.CONTACT_MODIFIER;
                case ALLIANCE:    return Tension.DROP_ALLIANCE_MODIFIER;
                case PEACE:       return 0;
                case CEASE_FIRE:  return Tension.PEACE_TREATY_MODIFIER;
                case WAR:         return Tension.CEASE_FIRE_MODIFIER + Tension.PEACE_TREATY_MODIFIER;
                default:          this.badStance();
                }
            case CEASE_FIRE:
                switch (this) {
                case UNCONTACTED: badTransition(newStance);
                case ALLIANCE:    badTransition(newStance);
                case PEACE:       badTransition(newStance);
                case CEASE_FIRE:  return 0;
                case WAR:         return Tension.CEASE_FIRE_MODIFIER;
                default:          this.badStance();
                }
            case WAR:
                switch (this) {
                case UNCONTACTED: return Tension.WAR_MODIFIER;
                case ALLIANCE:    return Tension.WAR_MODIFIER;
                case PEACE:       return Tension.WAR_MODIFIER;
                case CEASE_FIRE:  return Tension.RESUME_WAR_MODIFIER;
                case WAR:         return 0;
                default:          this.badStance();
                }
            default:
                throw new IllegalStateException("Bogus newStance");
            }
        }
    }


    /**
     * Only used by AI - stores the tension levels, 0-1000 with 1000 maximum
     * hostility.
     */
    protected java.util.Map<Player, Tension> tension
        = new HashMap<Player, Tension>();
    // TODO: move this to AIPlayer

    /**
     * Stores the stance towards the other players. One of: WAR, CEASE_FIRE,
     * PEACE and ALLIANCE.
     */
    protected java.util.Map<String, Stance> stance
        = new HashMap<String, Stance>();

    /**
     * The name of this player. This defaults to the user name in case of a
     * human player and the rulerName of the NationType in case of an AI player.
     */
    protected String name;

    /** The name of this player as an independent nation. */
    protected String independentNationName;

    /** The NationType of this player. */
    protected NationType nationType;

    /** The nation ID of this player, e.g. "model.nation.dutch". */
    protected String nationID;

    /** The name this player uses for the New World. */
    protected String newLandName = null;

    /** Is this player an admin? */
    protected boolean admin;

    /** The current score of this player. */
    protected int score;

    /** The amount of gold this player owns. */
    protected int gold;

    /**
     * The number of immigration points. Immigration points are an
     * abstract game concept. They are generated by but are not
     * identical to crosses.
     */
    protected int immigration;

    /**
     * The number of liberty points. Liberty points are an
     * abstract game concept. They are generated by but are not
     * identical to bells.
     */
    protected int liberty;

    /** The market for Europe. */
    protected Market market;

    /** The European port/location for this player. */
    protected Europe europe;

    /** The monarch for this player. */
    protected Monarch monarch;

    protected boolean ready;

    /** True if this is an AI player. */
    protected boolean ai;

    /** True if player has been attacked by privateers. */
    protected boolean attackedByPrivateers = false;

    /** SoL from last turn. */
    protected int oldSoL;

    /** Is this player dead? */
    protected boolean dead = false;

    /** The founding fathers in this Player's congress. */
    final protected Set<FoundingFather> allFathers
        = new HashSet<FoundingFather>();
    /** Current founding father being recruited. */
    protected FoundingFather currentFather;
    /** The offered founding fathers. */
    final protected List<FoundingFather> offeredFathers
        = new ArrayList<FoundingFather>();

    /** The current tax rate for this player. */
    protected int tax = 0;

    /** The type of player. */
    public static enum PlayerType {
        NATIVE, COLONIAL, REBEL, INDEPENDENT, ROYAL, UNDEAD
    }
    protected PlayerType playerType;

    protected int immigrationRequired = 12;

    protected Location entryLocation;

    /** The Units this player owns. */
    protected final java.util.Map<String, Unit> units
        = new HashMap<String, Unit>();

    /** The Settlements this player owns. */
    protected final List<Settlement> settlements
        = new ArrayList<Settlement>();

    /** Trade routes of this player. */
    protected final List<TradeRoute> tradeRoutes = new ArrayList<TradeRoute>();

    /** Model messages for this player. */
    protected final List<ModelMessage> modelMessages
        = new ArrayList<ModelMessage>();

    /** The history events occuring with this player. */
    protected final List<HistoryEvent> history = new ArrayList<HistoryEvent>();

    /** The last-sale data. */
    protected HashMap<String, LastSale> lastSales = null;

    /** Indices of largest used region name by type. */
    protected final HashMap<String, Integer> nameIndex
        = new HashMap<String, Integer>();

    // Temporary variables:

    // Tiles the player can see.
    // No access to canSeeTiles without taking canSeeLock.
    private boolean[][] canSeeTiles = null;
    private final Object canSeeLock = new Object();

    // Contains the abilities and modifiers of this type.
    protected FeatureContainer featureContainer;

    // Maximum food consumption of unit types available to this player.
    private int maximumFoodConsumption = -1;

    private final UnitIterator nextActiveUnitIterator
        = new UnitIterator(this, new ActivePredicate(this));

    private final UnitIterator nextGoingToUnitIterator
        = new UnitIterator(this, new GoingToPredicate(this));

    /**
     * The HighSeas is a Location that enables Units to travel between
     * the New World and one or several European Ports.
     */
    protected HighSeas highSeas;

    /**
     * A cache of settlement names, a capital for natives, and a fallback
     * settlement name prefix.
     * Does not need to be serialized.
     */
    protected List<String> settlementNames = null;
    protected String capitalName = null;
    protected String settlementFallback = null;


    public static final Comparator<Player> playerComparator = new Comparator<Player>() {
        public int compare(Player player1, Player player2) {
            int counter1 = 0;
            int counter2 = 0;
            if (player1.isAdmin()) {
                counter1 += 8;
            }
            if (!player1.isAI()) {
                counter1 += 4;
            }
            if (player1.isEuropean()) {
                counter1 += 2;
            }
            if (player2.isAdmin()) {
                counter2 += 8;
            }
            if (!player2.isAI()) {
                counter2 += 4;
            }
            if (player2.isEuropean()) {
                counter2 += 2;
            }

            return counter2 - counter1;
        }
    };


    /**
     * Constructor for ServerPlayer.
     */
    protected Player() {
        // empty
    }

    /**
     * Constructor for ServerPlayer.
     */
    protected Player(Game game) {
        super(game);
    }

    /**
     *
     * Initiates a new <code>Player</code> from an <code>Element</code> and
     * registers this <code>Player</code> at the specified game.
     *
     * @param game The <code>Game</code> this object belongs to.
     * @param in The input stream containing the XML.
     * @throws XMLStreamException if a problem was encountered during parsing.
     */
    public Player(Game game, XMLStreamReader in) throws XMLStreamException {
        super(game, in);
        readFromXML(in);
    }

    /**
     * Initiates a new <code>Player</code> from an <code>Element</code> and
     * registers this <code>Player</code> at the specified game.
     *
     * @param game The <code>Game</code> this object belongs to.
     * @param e An XML-element that will be used to initialize this object.
     *
     */
    public Player(Game game, Element e) {
        super(game, e);
        readFromXMLElement(e);
    }

    /**
     * Initiates a new <code>Player</code> with the given ID. The object
     * should later be initialized by calling either {@link
     * #readFromXML(XMLStreamReader)} or {@link #readFromXMLElement(Element)}.
     *
     * @param game The <code>Game</code> in which this object belong.
     * @param id The unique identifier for this object.
     */
    public Player(Game game, String id) {
        super(game, id);
    }

    /**
     * Get the <code>FeatureContainer</code> value.
     *
     * @return a <code>FeatureContainer</code> value
     */
    public final FeatureContainer getFeatureContainer() {
        return featureContainer;
    }

    /**
     * Set the <code>FeatureContainer</code> value.
     *
     * @param newFeatureContainer The new FeatureContainer value.
     */
    public final void setFeatureContainer(final FeatureContainer newFeatureContainer) {
        this.featureContainer = newFeatureContainer;
    }

    /**
     * Get the modifier set for a given id from the feature container.
     *
     * @param id The id to look up.
     * @return The modifier set.
     */
    public Set<Modifier> getModifierSet(String id) {
        return featureContainer.getModifierSet(id);
    }

    /**
     * Get the modifier set for a given id and type from the feature container.
     *
     * @param id The id to look up.
     * @param type The associated type.
     * @return The modifier set.
     */
    public Set<Modifier> getModifierSet(String id, FreeColGameObjectType type) {
        return featureContainer.getModifierSet(id, type);
    }

    /**
     * Does a player have a particular ability.
     *
     * @param ability The ability to test.
     * @return True if the player has the ability.
     */
    public boolean hasAbility(String ability) {
        return featureContainer.hasAbility(ability);
    }

    /**
     * Adds a <code>ModelMessage</code> for this player.
     *
     * @param modelMessage The <code>ModelMessage</code>.
     */
    public void addModelMessage(ModelMessage modelMessage) {
        modelMessage.setOwnerId(getId());
        modelMessages.add(modelMessage);
    }

    /**
     * Returns all ModelMessages for this player.
     *
     * @return all ModelMessages for this player.
     */
    public List<ModelMessage> getModelMessages() {
        return modelMessages;
    }

    /**
     * Returns all new ModelMessages for this player.
     *
     * @return all new ModelMessages for this player.
     */
    public List<ModelMessage> getNewModelMessages() {

        ArrayList<ModelMessage> out = new ArrayList<ModelMessage>();

        for (ModelMessage message : modelMessages) {
            if (message.hasBeenDisplayed()) {
                continue;
            } else {
                out.add(message); // preserve message order
            }
        }

        return out;
    }

    /**
     * Removes all undisplayed model messages for this player.
     */
    public void removeModelMessages() {
        Iterator<ModelMessage> messageIterator = modelMessages.iterator();
        while (messageIterator.hasNext()) {
            ModelMessage message = messageIterator.next();
            if (message.hasBeenDisplayed()) {
                messageIterator.remove();
            }
        }
    }

    /**
     * Removes all the model messages for this player.
     */
    public void clearModelMessages() {
        modelMessages.clear();
    }

    /**
     * Sometimes an event causes the source (and display) fields in an
     * accumulated model message to become invalid (e.g. Europe disappears
     * on independence).  This routine is for cleaning up such cases.
     *
     * @param source the source field that has become invalid
     * @param newSource a new source field to replace the old with, or
     *   if null then remove the message
     */
    public void divertModelMessages(FreeColGameObject source, FreeColGameObject newSource) {
        // Since we are changing the list, we need to copy it to be able
        // to iterate through it
        List<ModelMessage> modelMessagesList = new ArrayList<ModelMessage>();
        modelMessagesList.addAll(modelMessages);

        for (ModelMessage modelMessage : modelMessagesList) {
            if (modelMessage.getSourceId() == source.getId()) {
                if (newSource == null) {
                    modelMessages.remove(modelMessage);
                } else {
                    modelMessage.divert(newSource);
                }
            }
        }
    }

    /**
     * Returns the maximum food consumption of any unit types
     * available to this player.
     *
     * @return an <code>int</code> value
     */
    public int getMaximumFoodConsumption() {
        if (maximumFoodConsumption < 0) {
            for (UnitType unitType : getSpecification().getUnitTypeList()) {
                if (unitType.isAvailableTo(this)) {
                    int foodConsumption = 0;
                    for (GoodsType foodType : getSpecification().getFoodGoodsTypeList()) {
                        foodConsumption += unitType.getConsumptionOf(foodType);
                    }
                    if (foodConsumption > maximumFoodConsumption) {
                        maximumFoodConsumption = foodConsumption;
                    }
                }
            }
        }
        return maximumFoodConsumption;
    }

    /**
     * Returns the current score of the player.
     *
     * @return an <code>int</code> value
     */
    public int getScore() {
        return score;
    }

    /**
     * Set the current score of the player.
     *
     * @param newScore The new score.
     */
    public void setScore(int newScore) {
        score = newScore;
    }

    /**
     * Modifies the score of the player by the given value.
     *
     * @param value an <code>int</code> value
     */
    public void modifyScore(int value) {
        score += value;
    }

    /**
     * Returns this Player's Market.
     *
     * @return This Player's Market.
     */
    public Market getMarket() {
        return market;
    }

    /**
     * Resets this Player's Market.
     */
    public void reinitialiseMarket() {
        market = new Market(getGame(), this);
    }

    /**
     * What is the name of the player's market?
     * Following a declaration of independence we are assumed to trade
     * broadly with any European market rather than a specific port.
     *
     * @return A name for the player's market.
     */
    public StringTemplate getMarketName() {
        return (getEurope() == null) ? StringTemplate.key("model.market.independent")
            : StringTemplate.key(nationID + ".europe");
    }

    /**
     * Checks if this player owns the given <code>Settlement</code>.
     *
     * @param s The <code>Settlement</code>.
     * @return <code>true</code> if this <code>Player</code> owns the given
     *         <code>Settlement</code>.
     */
    public boolean hasSettlement(Settlement s) {
        return settlements.contains(s);
    }

    /**
     * Adds a given settlement to this player's list of settlements.
     *
     * @param settlement The <code>Settlement</code> to add.
     */
    public void addSettlement(Settlement settlement) {
        if (!hasSettlement(settlement)) {
            if (settlement.getOwner() != this) {
                throw new IllegalStateException("Player does not own settlement.");
            }
            settlements.add(settlement);
        }
    }

    /**
     * Removes the given settlement from this player's list of settlements.
     *
     * @param settlement The <code>Settlement</code> to remove.
     */
    public void removeSettlement(Settlement settlement) {
        if (hasSettlement(settlement)) {
            if (settlement.getOwner() == this) {
                throw new IllegalStateException("Player still owns settlement.");
            }
            settlements.remove(settlement);
        }
    }
    
    public boolean owns(Ownable ownable) {
        if (ownable == null)
            return false;
        return this.equals(ownable.getOwner());
    }
    
    

    /**
     * Returns a list of all Settlements this player owns.
     *
     * @return The settlements this player owns.
     */
    public List<Settlement> getSettlements() {
        return settlements;
    }

    /**
     * Get the number of settlements.
     *
     * @return The number of settlements this player has.
     */
    public int getNumberOfSettlements() {
        return settlements.size();
    }

    /**
     * Gets a fresh list of all colonies this player owns.
     *
     * @return A fresh list of the colonies this player owns.
     */
    public List<Colony> getColonies() {
        ArrayList<Colony> colonies = new ArrayList<Colony>();
        for (Settlement s : settlements) {
            if (s instanceof Colony) {
                colonies.add((Colony) s);
            } else {
                throw new RuntimeException("getColonies can only be called for players whose settlements are colonies.");
            }
        }
        return colonies;
    }

    /**
     * Returns a sorted list of all Colonies this player owns.
     *
     * @param c A comparator to operate on the colony list.
     * @return A fresh list of the colonies this player owns.
     */
    public List<Colony> getSortedColonies(Comparator<Colony> c) {
        List<Colony> colonies = getColonies();
        Collections.sort(colonies, c);
        return colonies;
    }

    /**
     * Returns the sum of units currently working in the colonies of
     * this player.
     *
     * @return Sum of units currently working in the colonies.
     */
    public int getColoniesPopulation() {
        int i = 0;
        for (Colony c : getColonies()) {
            i += c.getUnitCount();
        }
        return i;
    }

    /**
     * Returns the <code>Colony</code> with the given name.
     *
     * @param name The name of the <code>Colony</code>.
     * @return The <code>Colony</code> or <code>null</code> if this player
     *         does not have a <code>Colony</code> with the specified name.
     */
    public Colony getColony(String name) {
        for (Colony colony : getColonies()) {
            if (colony.getName().equals(name)) {
                return colony;
            }
        }
        return null;
    }

    /**
     * Returns a list of all IndianSettlements this player owns.
     *
     * @return The indian settlements this player owns.
     */
    public List<IndianSettlement> getIndianSettlements() {
        ArrayList<IndianSettlement> indianSettlements = new ArrayList<IndianSettlement>();
        for (Settlement s : settlements) {
            if (s instanceof IndianSettlement) {
                indianSettlements.add((IndianSettlement) s);
            } else {
                throw new RuntimeException("getIndianSettlements found: " + s);
            }
        }
        return indianSettlements;
    }

    /**
     * Returns the <code>IndianSettlement</code> with the given name.
     *
     * @param name The name of the <code>IndianSettlement</code>.
     * @return The <code>IndianSettlement</code> or <code>null</code> if this player
     *         does not have a <code>IndianSettlement</code> with the specified name.
     */
    public IndianSettlement getIndianSettlement(String name) {
        for (IndianSettlement settlement : getIndianSettlements()) {
            if (settlement.getName().equals(name)) {
                return settlement;
            }
        }
        return null;
    }

    /**
     * Returns a list of all IndianSettlements this player owns that have
     * missions, optionally owned by a specific player.
     *
     * @param other If non-null, collect only missions established by
     *     this <code>Player</code>
     * @return The settlements this player owns with the specified
     *     mission type.
     */
    public List<IndianSettlement> getIndianSettlementsWithMission(Player other) {
        ArrayList<IndianSettlement> indianSettlements
            = new ArrayList<IndianSettlement>();
        for (Settlement s : settlements) {
            Unit missionary;
            if (s instanceof IndianSettlement
                && (missionary = ((IndianSettlement)s).getMissionary()) != null
                && (other == null || missionary.getOwner() == other)) {
                indianSettlements.add((IndianSettlement) s);
            }
        }
        return indianSettlements;
    }

    /**
     * Find a <code>Settlement</code> by name.
     *
     * @param name The name of the <code>Settlement</code>.
     * @return The <code>Settlement</code>, or <code>null</code> if not found.
     **/
    public Settlement getSettlement(String name) {
        return (isIndian()) ? getIndianSettlement(name) : getColony(name);
    }

    /**
     * Installs suitable settlement names (and the capital if native)
     * into the player name cache.
     *
     * @param names A list of settlement names with the fallback prefix first.
     * @param random A <code>Random</code> number source.
     */
    public void installSettlementNames(List<String> names, Random random) {
        if (settlementNames == null) {
            settlementNames = new ArrayList<String>();
            settlementNames.addAll(names);
            settlementFallback = settlementNames.remove(0);
            if (isIndian()) {
                capitalName = settlementNames.remove(0);
                if (random != null) {
                    Collections.shuffle(settlementNames, random);
                }
            }
        }
        logger.info("Installed " + names.size()
                    + " settlement names for player " + this.toString());
    }

    /**
     * Gets the name of this players capital.  Only meaningful to natives.
     *
     * @return The name of this players capital.
     */
    public String getCapitalName() {
        return (capitalName == null) ? ASSIGN_SETTLEMENT_NAME : capitalName;
    }

    /**
     * Gets a settlement name suitable for this player.
     *
     * @return A new settlement name.
     */
    public String getSettlementName() {
        Game game = getGame();

        // ASSIGN_SETTLEMENT_NAME can be sent with buildColony and a
        // default name will be filled in.
        if (settlementNames == null) return ASSIGN_SETTLEMENT_NAME;

        while (!settlementNames.isEmpty()) {
            String name = settlementNames.remove(0);
            if (game.getSettlement(name) == null) return name;
        }

        // Fallback method
        final String base = settlementFallback + "-";
        String name;
        int i = settlements.size() + 1;
        while (game.getSettlement(name = base + Integer.toString(i)) != null) {
            i++;
        }
        return name;
    }

    /**
     * Returns the type of this player.
     *
     * @return The player type.
     */
    public PlayerType getPlayerType() {
        return playerType;
    }

    /**
     * Sets the player type.
     *
     * @param type The new player type.
     * @see #getPlayerType
     */
    public void setPlayerType(PlayerType type) {
        playerType = type;
    }

    /**
     * Checks if this player is european. This includes the "Royal Expeditionay
     * Force".
     *
     * @return <i>true</i> if this player is european and <i>false</i>
     *         otherwise.
     */
    public boolean isEuropean() {
        return nationType != null && nationType.isEuropean();
    }

    /**
     * Checks if this player is indian. This method returns the opposite of
     * {@link #isEuropean()}.
     *
     * @return <i>true</i> if this player is indian and <i>false</i>
     *         otherwise.
     */
    public boolean isIndian() {
        return playerType == PlayerType.NATIVE;
    }

    /**
     * Checks if this player is undead.
     *
     * @return True if this player is undead.
     */
    public boolean isUndead() {
        return playerType == PlayerType.UNDEAD;
    }

    /**
     * Checks if this player is a "royal expeditionary force.
     *
     * @return <code>true</code> is the given nation is a royal expeditionary
     *         force and <code>false</code> otherwise.
     */
    public boolean isREF() {
        return nationType != null && nationType.isREF();
    }

    /**
     * Determines whether this player is an AI player.
     *
     * @return Whether this player is an AI player.
     */
    public boolean isAI() {
        return ai;
    }

    /**
     * Sets whether this player is an AI player.
     *
     * @param ai <code>true</code> if this <code>Player</code> is controlled
     *            by the computer.
     */
    public void setAI(boolean ai) {
        this.ai = ai;
    }

    /**
     * Checks if this player is an admin.
     *
     * @return <i>true</i> if the player is an admin and <i>false</i>
     *         otherwise.
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Checks if this player is dead. A <code>Player</code> dies when it
     * loses the game.
     *
     * @return <code>true</code> if this <code>Player</code> is dead.
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Get the player death state.
     * This is indeed identical to isDead(), but is needed for partial
     * updates to complement the setDead() function.
     *
     * @return True if this <code>Player</code> is dead.
     */
    public boolean getDead() {
        return dead;
    }

    /**
     * Sets this player to be dead or not.
     *
     * @param dead Should be set to <code>true</code> when this
     *            <code>Player</code> dies.
     * @see #isDead
     */
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    /**
     * Checks whether this player is at war with any other player.
     *
     * @return <i>true</i> if this player is at war with any other.
     */
    public boolean isAtWar() {
        for (Player player : getGame().getPlayers()) {
            if (atWarWith(player)) return true;
        }
        return false;
    }

    /**
     * Checks if this player has work to do if it is a REF-player.
     *
     * @return True if any of our units are located in the new
     *     world or a nation is in rebellion against us.
     */
    public boolean isWorkForREF() {
        for (Unit u : getUnits()) { // Work to do if unit in the new world
            if (u.getTile() != null) return true;
        }
        return !getRebels().isEmpty();
    }

    /**
     * Gets a list of the players in rebellion against this (REF) player.
     *
     * @return A list of nations in rebellion against us.
     */
    public List<Player> getRebels() {
        List<Player> rebels = new ArrayList<Player>();
        for (Player p : getGame().getLiveEuropeanPlayers()) {
            if (p.getREFPlayer() == this
                && (p.getPlayerType() == PlayerType.REBEL
                    || p.getPlayerType() == PlayerType.UNDEAD)) rebels.add(p);
        }
        return rebels;
    }


    /**
     * A variety of reasons why a tile can not be claimed, either
     * to found a settlement or just to be used by one, including the
     * double negative NONE == "no reason" case.
     */
    public static enum NoClaimReason {
        NONE,            // Actually, tile can be claimed
        TERRAIN,         // Not on settleable terrain
        RUMOUR,          // Europeans can not claim tiles with LCR
        WATER,           // Natives do not claim water
        SETTLEMENT,      // Settlement present
        WORKED,          // One of our settlements is working this tile
        EUROPEANS,       // Owned by Europeans and not for sale
        NATIVES,         // Owned by natives and they want payment for it
    };

    /**
     * Can a tile be owned by this player?
     *
     * @param tile The <code>Tile</code> to consider.
     * @return True if the tile can be owned by this player.
     */
    public boolean canOwnTile(Tile tile) {
        return canOwnTileReason(tile) == NoClaimReason.NONE;
    }

    /**
     * Can a tile be owned by this player?
     * This is a test of basic practicality and does not consider
     * the full complexity of tile ownership issues.
     *
     * @param tile The <code>Tile</code> to consider.
     * @return The reason why/not the tile can be owned by this player.
     */
    private NoClaimReason canOwnTileReason(Tile tile) {
        return (isEuropean())
            ? ((tile.hasLostCityRumour())
               ? NoClaimReason.RUMOUR
               : NoClaimReason.NONE)
            : ((tile.isLand())
               ? NoClaimReason.NONE
               : NoClaimReason.WATER);
    }

    /**
     * Checks if a tile can be claimed for use by a settlement.
     *
     * @param tile The <code>Tile</code> to try to claim.
     * @return True if the tile can be claimed to found a settlement.
     */
    public boolean canClaimForSettlement(Tile tile) {
        return canClaimForSettlementReason(tile) == NoClaimReason.NONE;
    }

    /**
     * The test for whether a tile can be freely claimed by a player
     * settlement (freely => not by purchase or stealing).  The rule
     * for the center tile is different, see below.
     *
     * The tile must be ownable by this player, settlement-free, and
     * either not currently owned, owned by this player and not by
     * another settlement that is using the tile, or owned by someone
     * else who does not want anything for it.  Got that?
     *
     * @param tile The <code>Tile</code> to try to claim.
     * @return The reason why/not the tile can be claimed.
     */
    public NoClaimReason canClaimForSettlementReason(Tile tile) {
        int price;
        NoClaimReason reason = canOwnTileReason(tile);
        return (reason != NoClaimReason.NONE) ? reason
            : (tile.getSettlement() != null) ? NoClaimReason.SETTLEMENT
            : (tile.getOwner() == null) ? NoClaimReason.NONE
            : (tile.getOwner() == this) ? ((tile.isInUse())
                                           ? NoClaimReason.WORKED
                                           : NoClaimReason.NONE)
            : ((price = getLandPrice(tile)) < 0) ? NoClaimReason.EUROPEANS
            : (price > 0) ? NoClaimReason.NATIVES
            : NoClaimReason.NONE;
    }

    /**
     * Can a tile be claimed to found a settlement on?
     *
     * @param tile The <code>Tile</code> to try to claim.
     * @return True if the tile can be claimed to found a settlement.
     */
    public boolean canClaimToFoundSettlement(Tile tile) {
        return canClaimToFoundSettlementReason(tile) == NoClaimReason.NONE;
    }

    /**
     * Can a tile be claimed to found a settlement on?
     * Almost the same as canClaimForSettlement but there is an extra
     * requirement that the tile be of a settleable type, and some
     * relaxations that allow free center tile acquisition
     *
     * @param tile The <code>Tile</code> to try to claim.
     * @return The reason why/not the tile can be claimed.
     */
    public NoClaimReason canClaimToFoundSettlementReason(Tile tile) {
        NoClaimReason reason;
        return (!tile.getType().canSettle()) ? NoClaimReason.TERRAIN
            : ((reason = canClaimForSettlementReason(tile))
               != NoClaimReason.NATIVES) ? reason
            : (canClaimFreeCenterTile(tile)) ? NoClaimReason.NONE
            : NoClaimReason.NATIVES;
    }

    /**
     * Is this tile claimable for a colony center tile under the
     * special provisions of the model.option.buildOnNativeLand option.
     *
     * @param tile The <code>Tile</code> to try to claim.
     * @return True if the tile can be claimed.
     */
    private boolean canClaimFreeCenterTile(Tile tile) {
        String build = getGame().getSpecification()
            .getStringOption("model.option.buildOnNativeLand").getValue();
        return isEuropean()
            && tile.getOwner() != null
            && tile.getOwner().isIndian()
            && ("model.option.buildOnNativeLand.always".equals(build)
                || ("model.option.buildOnNativeLand.first".equals(build)
                    && hasZeroSettlements())
                || ("model.option.buildOnNativeLand.firstAndUncontacted".equals(build)
                    && hasZeroSettlements()
                    && (tile.getOwner() == null
                        || tile.getOwner().getStance(this)
                        == Stance.UNCONTACTED)));
    }

    /**
     * The second and third cases of buildOnNative land need to test
     * if the player has no settlements yet.  We can not just check
     * that the number of settlement is zero because by the time the
     * settlement is being placed and we are collecting the tiles to
     * claim, the settlement already exists and thus there will
     * already be one settlement--- so we have to check if that one
     * settlement is on the map yet.
     *
     * @return True if the player has no settlements (on the map) yet.
     */
    private boolean hasZeroSettlements() {
        List<Settlement> settlements = getSettlements();
        return settlements.isEmpty()
            || (settlements.size() == 1
                && settlements.get(0).getTile().getSettlement() == null);
    }

    /**
     * Can the ownership of this tile be claimed for the purposes of
     * making an improvement.  Quick test that does not handle the
     * curly case of tile transfer between colonies, or guarantee
     * success (natives may want to be paid), but just that success is
     * possible.
     *
     * @param tile The <code>Tile</code> to consider.
     *
     * @return True if the tile ownership can be claimed.
     */
    public boolean canClaimForImprovement(Tile tile) {
        Player owner = tile.getOwner();
        return owner == null || owner == this || getLandPrice(tile) == 0;
    }

    /**
     * Can a tile be acquired from its owners and used for an improvement?
     * Slightly weakens canClaimForImprovement to allow for purchase
     * and/or stealing.
     *
     * @param tile The <code>Tile</code> to consider.
     * @return True if the tile ownership can be claimed.
     */
    public boolean canAcquireForImprovement(Tile tile) {
        return canClaimForImprovement(tile)
            || getLandPrice(tile) > 0;
    }

    /**
     * Get the <code>Unit</code> value.
     *
     * @return a <code>List<Unit></code> value
     */
    public final Unit getUnit(String id) {
        return units.get(id);
    }

    /**
     * Set the <code>Unit</code> value.
     *
     * @param newUnit The new Units value.
     */
    public final void setUnit(final Unit newUnit) {
    	if (newUnit == null) {
    		logger.warning("Unit to add is null");
    		return;
    	}
    	
    	// make sure the owner of the unit is set first, before adding it to the list
    	if(newUnit.getOwner() != null && !this.owns(newUnit)){
    		throw new IllegalStateException(this + " adding another players unit=" + newUnit);
    	}

    	units.put(newUnit.getId(), newUnit);
    }

    /**
     * Remove Unit.
     *
     * @param oldUnit an <code>Unit</code> value
     */
    public void removeUnit(final Unit oldUnit) {
        if (oldUnit != null) {
            units.remove(oldUnit.getId());
        }
    }

    /**
     * Gets the price to this player for a proposed unit.
     *
     * @param au The proposed <code>AbstractUnit</code>.
     * @return The price for the unit.
     */
    public int getPrice(AbstractUnit au) {
        Specification spec = getSpecification();
        UnitType unitType = au.getUnitType(spec);
        if (unitType.hasPrice()) {
            int price = getEurope().getUnitPrice(unitType);
            for (EquipmentType equip : au.getEquipment(spec)) {
                for (AbstractGoods goods : equip.getGoodsRequired()) {
                    price += getMarket().getBidPrice(goods.getType(),
                                                     goods.getAmount());
                }
            }
            return price * au.getNumber();
        } else {
            return INFINITY;
        }
    }

    /**
     * Gets the total percentage of rebels in all this player's colonies.
     *
     * @return The total percentage of rebels in all this player's colonies.
     */
    public int getSoL() {
        int sum = 0;
        int number = 0;
        for (Colony c : getColonies()) {
            sum += c.getSoL();
            number++;
        }
        if (number > 0) {
            return sum / number;
        } else {
            return 0;
        }
    }

    /**
     * Get the <code>IndependentNationName</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getIndependentNationName() {
        return independentNationName;
    }

    /**
     * Set the <code>IndependentNationName</code> value.
     *
     * @param newIndependentNationName The new IndependentNationName value.
     */
    public final void setIndependentNationName(final String newIndependentNationName) {
        this.independentNationName = newIndependentNationName;
    }

    /**
     * Gets the <code>Player</code> controlling the "Royal Expeditionary
     * Force" for this player.
     *
     * @return The player, or <code>null</code> if this player does not have a
     *         royal expeditionary force.
     */
    public Player getREFPlayer() {
        Nation ref = getNation().getRefNation();
        return (ref == null) ? null : getGame().getPlayer(ref.getId());
    }

    /**
     * Gets the name this player has chosen for the new land.
     *
     * @return The name of the new world as chosen by the <code>Player</code>,
     *         or null if none chosen yet.
     */
    public String getNewLandName() {
        return newLandName;
    }

    /**
     * Returns true if the player already selected a new name for the discovered
     * land.
     *
     * @return true if the player already set a name for the newly discovered
     *         land, otherwise false.
     */
    public boolean isNewLandNamed() {
        return newLandName != null;
    }

    /**
     * Sets the name this player uses for the new land.
     *
     * @param newLandName This <code>Player</code>'s name for the new world.
     */
    public void setNewLandName(String newLandName) {
        this.newLandName = newLandName;
    }

    /**
     * Returns the price of the given land.
     *
     * @param tile The <code>Tile</code> to get the price for.
     * @return The price of the land if it is for sale, zero if it is already
     *         ours, unclaimed or unwanted, negative if it is not for sale.
     */
    public int getLandPrice(Tile tile) {
        Player nationOwner = tile.getOwner();
        int price = 0;

        if (nationOwner == null || nationOwner == this) {
            return 0; // Freely available
        } else if (tile.getSettlement() != null) {
            return -1; // Not for sale
        } else if (nationOwner.isEuropean()) {
            if (tile.getOwningSettlement() != null
                && tile.getOwningSettlement().getOwner() == nationOwner) {
                return -1; // Nailed down by a European colony
            } else {
                return 0; // Claim abandoned or only by tile improvement
            }
        } // Else, native ownership
        for (GoodsType type : getSpecification().getGoodsTypeList()) {
            if (type == getSpecification().getPrimaryFoodType()) {
                // Only consider specific food types, not the aggregation.
                continue;
            }
            price += tile.potential(type, null);
        }
        price *= getSpecification().getIntegerOption("model.option.landPriceFactor").getValue();
        price += 100;
        return (int) featureContainer
            .applyModifier(price, "model.modifier.landPaymentModifier",
                           null, getGame().getTurn());
    }

    /**
     * Returns whether this player has been attacked by privateers.
     *
     * @return <code>true</code> if this <code>Player</code> has been
     *         attacked by privateers.
     */
    public boolean getAttackedByPrivateers() {
        return attackedByPrivateers;
    }

    /**
     * Sets whether this player has been attacked by privateers.
     *
     * @param attacked True if the player has been attacked by privateers.
     */
    public void setAttackedByPrivateers(boolean attacked) {
        attackedByPrivateers = attacked;
    }

    /**
     * Gets the default <code>Location</code> where the units arriving from
     * {@link Europe} will be put.
     *
     * @return The <code>Location</code>.
     * @see Unit#getEntryLocation
     */
    public Location getEntryLocation() {
        return entryLocation;
    }

    /**
     * Sets the <code>Location</code> where the units arriving from
     * {@link Europe} will be put as a default.
     *
     * @param entryLocation The <code>Location</code>.
     * @see #getEntryLocation
     */
    public void setEntryLocation(Location entryLocation) {
        this.entryLocation = entryLocation;
    }

    /**
     * Checks if this <code>Player</code> has explored the given
     * <code>Tile</code>.
     *
     * @param tile The <code>Tile</code>.
     * @return <i>true</i> if the <code>Tile</code> has been explored and
     *         <i>false</i> otherwise.
     */
    public boolean hasExplored(Tile tile) {
        return tile.isExplored();
    }

    /**
     * Sets the given tile to be explored by this player and updates the
     * player's information about the tile.
     *
     * @param tile The <code>Tile</code> to set explored.
     * @see Tile#updatePlayerExploredTile(Player, boolean)
     */
    public void setExplored(Tile tile) {
        logger.warning("Implemented by ServerPlayer");
    }

    /**
     * Sets the tiles within the given <code>Unit</code>'s line of sight to
     * be explored by this player.
     *
     * @param unit The <code>Unit</code>.
     * @see #setExplored(Tile)
     * @see #hasExplored
     */
    public void setExplored(Unit unit) {
        if (getGame() == null || getGame().getMap() == null || unit == null
            || unit.getLocation() == null || unit.getTile() == null
            || isIndian()) {
            return;
        }
        invalidateCanSeeTiles();
    }

    /**
     * Forces an update of the <code>canSeeTiles</code>. This method should
     * be used to invalidate the current <code>canSeeTiles</code>. The method
     * {@link #resetCanSeeTiles} will be called whenever it is needed.
     */
    public void invalidateCanSeeTiles() {
        synchronized (canSeeLock) {
            canSeeTiles = null;
        }
    }

    /**
     * Checks if this <code>Player</code> can see the given <code>Tile</code>.
     * The <code>Tile</code> can be seen if it is in a {@link Unit}'s line of
     * sight.
     *
     * @param tile The given <code>Tile</code>.
     * @return <i>true</i> if the <code>Player</code> can see the given
     *         <code>Tile</code> and <i>false</i> otherwise.
     */
    public boolean canSee(Tile tile) {
        if (tile == null) return false;

        do {
            synchronized (canSeeLock) {
                if (canSeeTiles != null) {
                    return canSeeTiles[tile.getX()][tile.getY()];
                }
            }
        } while (resetCanSeeTiles());
        return false;
    }

    /**
     * Resets this player's "can see"-tiles. This is done by setting
     * all the tiles within each {@link Unit} and {@link Settlement}s
     * line of sight visible. The other tiles are made invisible.
     *
     * Use {@link #invalidateCanSeeTiles} whenever possible.
     * @return <code>true</code> if successful <code>false</code> otherwise
     */
    private boolean resetCanSeeTiles() {
        Map map = getGame().getMap();
        if (map == null) return false;

        boolean[][] cST = makeCanSeeTiles(map);
        synchronized (canSeeLock) {
            canSeeTiles = cST;
        }
        return true;
    }

    /**
     * Builds a canSeeTiles array.
     *
     * Note that tiles must be tested for null as they may be both
     * valid tiles but yet null during a save game load.
     *
     * Note the use of copies of the unit and settlement lists to
     * avoid nasty surprises due to asynchronous disappearance of
     * members of either.  TODO: see if this can be relaxed.
     *
     * @param map The <code>Map</code> to use.
     * @return A canSeeTiles array.
     */
    private boolean[][] makeCanSeeTiles(Map map) {
        boolean[][] cST = new boolean[map.getWidth()][map.getHeight()];

        if (!getSpecification().getBoolean(GameOptions.FOG_OF_WAR)) {
            for (Tile t : getGame().getMap().getAllTiles()) {
                if (t != null) {
                    cST[t.getX()][t.getY()] = hasExplored(t);
                }
            }
        } else {
            for (Unit unit : getUnits()) {
                // Only consider units directly on the map, not those
                // on a carrier or in Europe.
                if (!(unit.getLocation() instanceof Tile)) continue;

                Tile tile = (Tile) unit.getLocation();
                cST[tile.getX()][tile.getY()] = true;
                for (Tile t : tile.getSurroundingTiles(unit.getLineOfSight())) {
                    if (t != null) {
                        cST[t.getX()][t.getY()] = hasExplored(t);
                    }
                }
            }
            for (Settlement settlement : new ArrayList<Settlement>(getSettlements())) {
                Tile tile = settlement.getTile();
                cST[tile.getX()][tile.getY()] = true;
                for (Tile t : tile.getSurroundingTiles(settlement.getLineOfSight())) {
                    if (t != null) {
                        cST[t.getX()][t.getY()] = hasExplored(t);
                    }
                }
            }
            if (isEuropean() && getSpecification()
                .getBoolean("model.option.enhancedMissionaries")) {
                for (Player other : getGame().getPlayers()) {
                    if (this.equals(other) || !other.isIndian()) continue;
                    for (Settlement settlement : other.getSettlements()) {
                        IndianSettlement is = (IndianSettlement) settlement;
                        if (is.getMissionary(this) != null) {
                            for (Tile t : is.getTile().getSurroundingTiles(is.getLineOfSight())) {
                                if (t != null) {
                                    cST[t.getX()][t.getY()] = hasExplored(t);
                                }
                            }
                        }
                    }
                }
            }
        }
        return cST;
    }

    /**
     * Checks if this <code>Player</code> can build colonies.
     *
     * @return <code>true</code> if this player is european, not the royal
     *         expeditionary force and not currently fighting the war of
     *         independence.
     */
    public boolean canBuildColonies() {
        return nationType.hasAbility("model.ability.foundColony");
    }

    /**
     * Checks if this <code>Player</code> can get founding fathers.
     *
     * @return <code>true</code> if this player is european, not the royal
     *         expeditionary force and not currently fighting the war of
     *         independence.
     */
    public boolean canHaveFoundingFathers() {
        return nationType.hasAbility("model.ability.electFoundingFather");
    }

    /**
     * Determines whether this player has a certain Founding father.
     *
     * @param someFather a <code>FoundingFather</code> value
     * @return Whether this player has this Founding father
     * @see FoundingFather
     */
    public boolean hasFather(FoundingFather someFather) {
        return allFathers.contains(someFather);
    }

    /**
     * Returns the number of founding fathers in this players congress. Used to
     * calculate number of liberty needed to recruit new fathers.
     *
     * @return The number of founding fathers in this players congress
     */
    public int getFatherCount() {
        return allFathers.size();
    }

    /**
     * Returns the founding fathers in this player's congress.
     *
     * @return the founding fathers in this player's congress.
     */
    public Set<FoundingFather> getFathers() {
        return allFathers;
    }

    /**
     * Add a founding father to the congress.
     *
     * @param father The <code>FoundingFather</code> to add.
     */
    public void addFather(FoundingFather father) {
        allFathers.add(father);
        featureContainer.add(father.getFeatureContainer());
        for (Colony colony : getColonies()) {
            colony.invalidateCache();
        }
    }

    /**
     * Gets the {@link FoundingFather founding father} this player is working
     * towards.
     *
     * @return The current FoundingFather or null if there is none
     * @see #setCurrentFather
     * @see FoundingFather
     */
    public FoundingFather getCurrentFather() {
        return currentFather;
    }

    /**
     * Sets this players liberty bell production to work towards recruiting
     * <code>father</code> to its congress.
     *
     * @param someFather a <code>FoundingFather</code> value
     * @see FoundingFather
     */
    public void setCurrentFather(FoundingFather someFather) {
        currentFather = someFather;
    }

    /**
     * Gets the set of offered fathers for this player.
     *
     * @return The current set of offered fathers.
     */
    public List<FoundingFather> getOfferedFathers() {
        return offeredFathers;
    }

    /**
     * Clear the set of offered fathers.
     */
    public void clearOfferedFathers() {
        offeredFathers.clear();
    }

    /**
     * Sets the set of offered fathers.
     *
     * @param fathers A list of <code>FoundingFather</code>s to offer.
     */
    public void setOfferedFathers(List<FoundingFather> fathers) {
        clearOfferedFathers();
        offeredFathers.addAll(fathers);
    }

    /**
     * Gets the number of liberty points needed to recruit the next
     * founding father.
     *
     * @return How many more liberty points the <code>Player</code>
     *         needs in order to recruit the next founding father.
     * @see #incrementLiberty
     */
    public int getRemainingFoundingFatherCost() {
        return getTotalFoundingFatherCost() - getLiberty();
    }

    /**
     * Returns how many liberty points in total are needed to earn the
     * Founding Father we are trying to recruit. The description of the
     * algorithm was taken from
     * http://t-a-w.blogspot.com/2007/05/colonization-tips.html
     *
     * @return Total number of liberty points the <code>Player</code>
     *         needs to recruit the next founding father.
     * @see #incrementLiberty
     */
    public int getTotalFoundingFatherCost() {
        int base = getSpecification()
            .getIntegerOption("model.option.foundingFatherFactor").getValue();
        int count = getFatherCount();
        return ((count + 1) * (count + 2) - 1) * base + count;
    }

    /**
     * Checks if this <code>Player</code> can move units to
     * <code>Europe</code>.
     *
     * @return <code>true</code> if this <code>Player</code> has an instance
     *         of <code>Europe</code>.
     */
    public boolean canMoveToEurope() {
        return getEurope() != null;
    }

    /**
     * Returns the europe object that this player has.
     *
     * @return The europe object that this player has or <code>null</code> if
     *         this <code>Player</code> does not have an instance
     *         <code>Europe</code>.
     */
    public Europe getEurope() {
        return europe;
    }

    /**
     * Set the europe object for a player.
     *
     * @param europe The new <code>Europe</code> object.
     */
    public void setEurope(Europe europe) {
        this.europe = europe;
    }

    /**
     * Describe <code>getEuropeName</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getEuropeNameKey() {
        if (europe == null) {
            return null;
        } else {
            return nationID + ".europe";
        }
    }

    /**
     * Returns the monarch object this player has.
     *
     * @return The monarch object this player has or <code>null</code> if this
     *         <code>Player</code> does not have an instance
     *         <code>Monarch</code>.
     */
    public Monarch getMonarch() {
        return monarch;
    }

    /**
     * Sets the monarch object this player has.
     *
     * @param monarch The monarch object this player should have.
     */
    public void setMonarch(Monarch monarch) {
        this.monarch = monarch;
    }

    /**
     * Get the <code>HighSeas</code> value.
     *
     * @return a <code>HighSeas</code> value
     */
    public final HighSeas getHighSeas() {
        return highSeas;
    }

    /**
     * Initialize the highSeas.
     * Needs to be public until the backward compatibility code in
     * FreeColServer is gone.
     */
    public void initializeHighSeas() {
        Game game = getGame();
        highSeas = new HighSeas(game);
        if (europe != null) highSeas.addDestination(europe);
        if (game.getMap() != null ) highSeas.addDestination(game.getMap());
    }

    /**
     * Returns the amount of gold that this player has.
     *
     * @return The amount of gold that this player has.  May return
     *     GOLD_NOT_ACCOUNTED for players whose gold is not accounted.
     */
    public int getGold() {
        return gold;
    }

    /**
     * Set the amount of gold that this player has.
     *
     * @param newGold The new player gold value.
     */
    public void setGold(int newGold) {
        gold = newGold;
    }

    /**
     * Checks if the player has enough gold to make a purchase.
     * Use this rather than comparing with getGold(), as this handles
     * players that do not account for gold.
     *
     * @param amount The purchase price to check.
     * @return True if the player can afford the purchase.
     */
    public boolean checkGold(int amount) {
        return this.gold == GOLD_NOT_ACCOUNTED || this.gold >= amount;
    }

    /**
     * Modifies the amount of gold that this player has. The argument can be
     * both positive and negative.
     *
     * @param amount The amount of gold to be added to this player.
     * @return The amount of gold post-modification.
     */
    public int modifyGold(int amount) {
        if (this.gold != Player.GOLD_NOT_ACCOUNTED) {
            if ((gold + amount) >= 0) {
                modifyScore((gold + amount) / 1000 - gold / 1000);
                gold += amount;
            } else {
                // This can happen if the server and the client get
                // out of sync.  Perhaps it can also happen if the
                // client tries to adjust gold for another player,
                // where the balance is unknown. Just keep going and
                // do the best thing possible, we don't want to crash
                // the game here.
                logger.warning("Cannot add " + amount + " gold for "
                               + this + ": would be negative!");
                gold = 0;
            }
        }
        return gold;
    }

    /**
     * Gets an <code>Iterator</code> containing all the units this player
     * owns.
     *
     * @return The <code>Iterator</code>.
     * @see Unit
     */
    public Iterator<Unit> getUnitIterator() {
        return units.values().iterator();
    }

    public List<Unit> getUnits() {
        return new ArrayList<Unit>(units.values());
    }

    /**
     * Gets the number of King's land units.
     * @return The number of units
     */
    public int getNumberOfKingLandUnits() {
        int n = 0;
        for (Unit unit : getUnits()) {
            if (unit.hasAbility("model.ability.refUnit") && !unit.isNaval()) {
                n++;
            }
        }
        return n;
    }

    /**
     * Checks if this player has a single Man-of-War.
     * @return <code>true</code> if this player owns
     *      a single Man-of-War.
     */
    public boolean hasManOfWar() {
        Iterator<Unit> it = getUnitIterator();
        while (it.hasNext()) {
            Unit unit = it.next();
            if ("model.unit.manOWar".equals(unit.getType().getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a new active unit.
     *
     * @return A <code>Unit</code> that can be made active.
     */
    public Unit getNextActiveUnit() {
        return nextActiveUnitIterator.next();
    }

    /**
     * Sets a new active unit.
     *
     * @param unit A <code>Unit</code> to make the next one to be active.
     * @return True if the operation succeeded.
     */
    public boolean setNextActiveUnit(Unit unit) {
        return nextActiveUnitIterator.setNext(unit);
    }

    /**
     * Gets a new going_to unit.
     *
     * @return A <code>Unit</code> that can be made active.
     */
    public Unit getNextGoingToUnit() {
        return nextGoingToUnitIterator.next();
    }

    /**
     * Checks if a new active unit can be made active.
     *
     * @return <i>true</i> if this is the case and <i>false</i> otherwise.
     */
    public boolean hasNextActiveUnit() {
        return nextActiveUnitIterator.hasNext();
    }

    /**
     * Checks if a new active unit can be made active.
     *
     * @return <i>true</i> if this is the case and <i>false</i> otherwise.
     */
    public boolean hasNextGoingToUnit() {
        return nextGoingToUnitIterator.hasNext();
    }

    /**
     * Returns the name of this player.
     *
     * @return The name of this player.
     */
    public String getName() {
        return name;
    }

    // TODO: remove this again
    public String getNameKey() {
        return getName();
    }

    /**
     * Gets the name to display for this player.
     * TODO: This is a kludge that should be fixed.
     *
     * @return The name to display for this player.
     */
    public String getDisplayName() {
        return (getName().startsWith("model.nation."))
            ? Messages.message(getName())
            : getName();
    }

    /**
     * Is this player the unknown enemy?
     */
    public boolean isUnknownEnemy() {
        return UNKNOWN_ENEMY.equals(name);
    }

    /**
     * Returns the name of this player.
     *
     * @return The name of this player.
     */
    public String toString() {
        return getName() + " (" + nationID + ")";
    }

    /**
     * Set the <code>Name</code> value.
     *
     * @param newName The new Name value.
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Returns the nation type of this player.
     *
     * @return The nation type of this player.
     */
    public NationType getNationType() {
        return nationType;
    }

    /**
     * Sets the nation type of this player.
     *
     * @param newNationType a <code>NationType</code> value
     */
    public void setNationType(NationType newNationType) {
        if (nationType != null) {
            featureContainer.remove(nationType.getFeatureContainer());
        }
        nationType = newNationType;
        featureContainer.add(newNationType.getFeatureContainer());
    }

    /**
     * Return this Player's nation.
     *
     * @return a <code>String</code> value
     */
    public Nation getNation() {
        return getSpecification().getNation(nationID);
    }

    /**
     * Sets the nation for this player.
     *
     * @param newNation The new nation for this player.
     */
    public void setNation(Nation newNation) {
        Nation oldNation = getNation();
        nationID = newNation.getId();
        getGame().getNationOptions().getNations().put(newNation, NationState.NOT_AVAILABLE);
        getGame().getNationOptions().getNations().put(oldNation, NationState.AVAILABLE);
    }

    /**
     * Return the ID of this Player's nation.
     *
     * @return a <code>String</code> value
     */
    public String getNationID() {
        return nationID;
    }

    /**
     * Gets a nation name suitable for use in message IDs.
     *
     * @return a <code>String</code> value
     */
    public String getNationNameKey() {
        return nationID.substring(nationID.lastIndexOf('.')+1)
            .toUpperCase(Locale.US);
    }

    /**
     * Returns the nation of this player as a String.
     *
     * @return The nation of this player as a String.
     */
    public StringTemplate getNationName() {
        return (playerType == PlayerType.REBEL
                || playerType == PlayerType.INDEPENDENT)
            ? StringTemplate.name(independentNationName)
            : StringTemplate.key(nationID + ".name");
    }

    /**
     * Get the <code>RulerName</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getRulerNameKey() {
        return nationID + ".ruler";
    }

    /**
     * Checks if this <code>Player</code> is ready to start the game.
     *
     * @return <code>true</code> if this <code>Player</code> is ready to
     *         start the game.
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Sets this <code>Player</code> to be ready/not ready for starting the
     * game.
     *
     * @param ready This indicates if the player is ready to start the game.
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void incrementImmigration(int amount) {
        immigration = Math.max(0, immigration + amount);
    }

    /**
     * Sets the number of immigration this player possess.
     *
     * @see #incrementImmigration(int)
     */
    public void reduceImmigration() {
        if (!canRecruitUnits()) {
            return;
        }

        int cost = getSpecification().getBoolean(GameOptions.SAVE_PRODUCTION_OVERFLOW)
            ? immigrationRequired : immigration;

        if (cost > immigration) {
            immigration = 0;
        } else {
            immigration -= cost;
        }
    }

    /**
     * Gets the number of immigration this player possess.
     *
     * @return The number.
     * @see #reduceImmigration
     */
    public int getImmigration() {
        return (canRecruitUnits()) ? immigration : 0;
    }

    /**
     * Sets the number of immigration this player possess.
     *
     * @param immigration The immigration value for this player.
     */
    public void setImmigration(int immigration) {
        if (canRecruitUnits()) {
            this.immigration = immigration;
        }
    }


    /**
     * Get the <code>TradeRoutes</code> value.
     *
     * @return a <code>List<TradeRoute></code> value
     */
    public final List<TradeRoute> getTradeRoutes() {
        return tradeRoutes;
    }

    /**
     * Set the players trade routes.
     *
     * @param newTradeRoutes The new list of <code>TradeRoute</code>s.
     *
     */
    public final void setTradeRoutes(final List<TradeRoute> newTradeRoutes) {
        tradeRoutes.clear();
        tradeRoutes.addAll(newTradeRoutes);
    }

    /**
     * Checks to see whether or not a colonist can emigrate, and does so if
     * possible.
     *
     * @return Whether a new colonist should immigrate.
     */
    public boolean checkEmigrate() {
        if (!canRecruitUnits()) {
            return false;
        }
        return getImmigrationRequired() <= immigration;
    }

    /**
     * Gets the number of immigration required to cause a new colonist to emigrate.
     *
     * @return The number of immigration required to cause a new colonist to
     *         emigrate.
     */
    public int getImmigrationRequired() {
        return (canRecruitUnits()) ? immigrationRequired : 0;
    }

    /**
     * Sets the number of immigration required to cause a new colonist to emigrate.
     *
     * @param immigrationRequired The number of immigration required to cause a new
     *            colonist to emigrate.
     */
    public void setImmigrationRequired(int immigrationRequired) {
        if (canRecruitUnits()) {
            this.immigrationRequired = immigrationRequired;
        }
    }

    /**
     * Updates the amount of immigration needed to emigrate a <code>Unit</code>
     * from <code>Europe</code>.
     */
    public void updateImmigrationRequired() {
        if (!canRecruitUnits()) {
            return;
        }
        immigrationRequired += (int) featureContainer
            .applyModifier(getSpecification()
                           .getIntegerOption("model.option.crossesIncrement").getValue(),
                           "model.modifier.religiousUnrestBonus");
        // The book I have tells me the crosses needed is:
        // [(colonist count in colonies + total colonist count) * 2] + 8.
        // So every unit counts as 2 unless they're in a colony,
        // wherein they count as 4.
        /*
         * int count = 8; Map map = getGame().getMap(); Iterator<Position>
         * tileIterator = map.getWholeMapIterator(); while
         * (tileIterator.hasNext()) { Tile t = map.getTile(tileIterator.next());
         * if (t != null && t.getFirstUnit() != null &&
         * t.getFirstUnit().getOwner().equals(this)) { Iterator<Unit>
         * unitIterator = t.getUnitIterator(); while (unitIterator.hasNext()) {
         * Unit u = unitIterator.next(); Iterator<Unit> childUnitIterator =
         * u.getUnitIterator(); while (childUnitIterator.hasNext()) { // Unit
         * childUnit = (Unit) childUnitIterator.next();
         * childUnitIterator.next(); count += 2; } count += 2; } } if (t != null &&
         * t.getColony() != null && t.getColony().getOwner() == this) { count +=
         * t.getColony().getUnitCount() * 4; // Units in colonies // count
         * doubly. // -sjm } } Iterator<Unit> europeUnitIterator =
         * getEurope().getUnitIterator(); while (europeUnitIterator.hasNext()) {
         * europeUnitIterator.next(); count += 2; } if (nation == ENGLISH) {
         * count = (count * 2) / 3; } setCrossesRequired(count);
         */
    }

    /**
     * Checks if this <code>Player</code> can recruit units by producing
     * immigration.
     *
     * @return <code>true</code> if units can be recruited by this
     *         <code>Player</code>.
     */
    public boolean canRecruitUnits() {
        return playerType == PlayerType.COLONIAL;
    }

    /**
     * Modifies the hostility against the given player.
     *
     * @param player The <code>Player</code>.
     * @param addToTension The amount to add to the current tension level.
     * @return A list of objects that may need updating due to the tension
     *     change (such as native settlements).
     */
    public List<FreeColGameObject> modifyTension(Player player,
                                                 int addToTension) {
        return modifyTension(player, addToTension, null);
    }

    /**
     * Modifies the hostility against the given player.
     *
     * @param player The <code>Player</code>.
     * @param addToTension The amount to add to the current tension level.
     * @param origin A <code>Settlement</code> where the alarming event
     *     occurred.
     * @return A list of objects that may need updating due to the tension
     *     change (such as native settlements).
     */
    public List<FreeColGameObject> modifyTension(Player player,
                                                 int addToTension,
                                                 Settlement origin) {
        if (player == null) {
            throw new IllegalStateException("Null player");
        } else if (player == this) {
            throw new IllegalStateException("Self tension!");
        } else if (origin != null && origin.getOwner() != this) {
            throw new IllegalStateException("Bogus origin:"
                                            + origin.getId());
        }

        List<FreeColGameObject> objects = new ArrayList<FreeColGameObject>();
        Tension.Level oldLevel = getTension(player).getLevel();
        getTension(player).modify(addToTension);
        if (oldLevel != getTension(player).getLevel()) {
            objects.add(this);
        }

        // Propagate tension change as settlement alarm to all
        // settlements except the one that originated it (if any).
        for (Settlement settlement : settlements) {
            if (!settlement.equals(origin)) {
                if (settlement.propagateAlarm(player, addToTension)) {
                    objects.add(settlement);
                }
            }
        }

        return objects;
    }

    /**
     * Sets the hostility against the given player.
     *
     * @param player The <code>Player</code>.
     * @param newTension The <code>Tension</code>.
     */
    public void setTension(Player player, Tension newTension) {
        if (player == this || player == null) {
            return;
        }
        tension.put(player, newTension);
    }

    /**
     * Gets the hostility this player has against the given player.
     *
     * @param player The <code>Player</code>.
     * @return An object representing the tension level.
     */
    public Tension getTension(Player player) {
        if (player == null) {
            throw new IllegalStateException("Null player.");
        } else {
            Tension newTension = tension.get(player);
            if (newTension == null) {
                newTension = new Tension(Tension.TENSION_MIN);
            }
            tension.put(player, newTension);
            return newTension;
        }
    }

    /**
     * Removes all tension with respect to a given player.  Used when a
     * player leaves the game.
     *
     * @param player The <code>Player</code> to remove tension for.
     */
    public void removeTension(Player player) {
        if (player != null) tension.remove(player);
    }

    /**
     * Get the <code>History</code> value.
     *
     * @return a <code>List<HistoryEvent></code> value
     */
    public final List<HistoryEvent> getHistory() {
        return history;
    }

    /**
     * Calculates the value of an outpost-type colony at this tile.
     * An "outpost" is supposed to be a colony containing one worker, exporting
     * its whole production to europe. The value of such colony is the maximum
     * amount of money it can make in one turn, assuming sale of its secondary
     * goods plus farmed goods from one of the surrounding tiles.
     *     
     * @return The value of a future colony located on this tile. This value is
     *         used by the AI when deciding where to build a new colony.
     */
    public int getOutpostValue(Tile t) {
        Market market = getMarket();
        if (canClaimToFoundSettlement(t)) {
            boolean nearbyTileIsOcean = false;
            float advantages = 1f;
            int value = 0;
            for (Tile tile : t.getSurroundingTiles(1)) {
                if (tile.getColony() != null) {
                    // can't build next to colony
                    return 0;
                } else if (tile.getSettlement() != null) {
                    // can build next to an indian settlement, but shouldn't
                    SettlementType type = tile.getSettlement().getType();
                    if (type.getClaimableRadius() > 1) {
                        // really shouldn't build next to cities
                        advantages *= 0.25f;
                    } else {
                        advantages *= 0.5f;
                    }
                } else {
                    if (tile.isConnected()) {
                        nearbyTileIsOcean = true;
                    }
                    if (tile.getType()!=null) {
                        for (AbstractGoods production : tile.getType().getProduction()) {
                            GoodsType type = production.getType();
                            int potential = market.getSalePrice(type, tile.potential(type, null));
                            if (tile.getOwner() != null &&
                                !this.owns(tile)) {
                                // tile is already owned by someone (and not by us!)
                                if (tile.getOwner().isEuropean()) {
                                    continue;
                                } else {
                                    potential /= 2;
                                }
                            }
                            value = Math.max(value, potential);
                        }
                    }
                }
            }

            //add secondary goods being produced by a colony on this tile
            if (t.getType().getSecondaryGoods() != null) {
                GoodsType secondary = t.getType().getSecondaryGoods().getType();
                value += market.getSalePrice(secondary,t.potential(secondary, null));
            }
            if (nearbyTileIsOcean) {
                return Math.max(0, (int) (value * advantages));
            }
        }
        return 0;
    }

    /**
     * Gets the value of building a <code>Colony</code> on the given tile.
     * This method adds bonuses to the colony value if the tile is close to (but
     * not overlapping with) another friendly colony. Penalties for enemy
     * units/colonies are added as well.
     *
     * @param t The <code>Tile</code>
     * @return The value of building a colony on the given tile.
     */
    public int getColonyValue(Tile t) {
        //----- TODO: tune magic numbers
        //applied once
        final float MOD_HAS_RESOURCE           = 0.75f;
        final float MOD_NO_PATH                = 0.5f;
        final float MOD_LONG_PATH              = 0.75f;
        final float MOD_FOOD_LOW               = 0.75f;
        final float MOD_FOOD_VERY_LOW          = 0.5f;

        //applied per goods
        final float MOD_BUILD_MATERIAL_MISSING = 0.10f;

        //applied per surrounding tile
        final float MOD_ADJ_SETTLEMENT_BIG     = 0.25f;
        final float MOD_ADJ_SETTLEMENT         = 0.5f;
        final float MOD_OWNED_EUROPEAN         = 0.8f;
        final float MOD_OWNED_NATIVE           = 0.9f;

        //applied per goods production, per surrounding tile
        final float MOD_HIGH_PRODUCTION        = 1.2f;
        final float MOD_GOOD_PRODUCTION        = 1.1f;

        //applied per occurrence (own colony only one-time), range-dependent.
        final float[] MOD_OWN_COLONY     = {0.0f, 0.0f, 0.5f, 1.25f, 1.1f};
        final float[] MOD_ENEMY_COLONY   = {0.0f, 0.0f, 0.6f, 0.7f,  0.8f};
        final float[] MOD_NEUTRAL_COLONY = {0.0f, 0.0f, 0.9f, 0.95f, 1.0f};
        final float[] MOD_ENEMY_UNIT     = {0.0f, 0.5f, 0.6f, 0.7f,  0.8f};

        final int LONG_PATH_TURNS = 3;
        final int PRIMARY_GOODS_VALUE = 30;

        //goods production in excess of this on a tile counts as good/high
        final int GOOD_PRODUCTION = 4;
        final int HIGH_PRODUCTION = 8;

        //counting "high" production as 2, "good" production as 1
        //overall food production is considered low/very low if less than...
        final int FOOD_LOW = 4;
        final int FOOD_VERY_LOW = 2;

        //----- END MAGIC NUMBERS

        // Return -INFINITY if there is a settlement here or neighbouring.
        for (Tile tile : t.getSurroundingTiles(0, 1)) {
            if (tile.getSettlement() != null) return -INFINITY;
        }

        //initialize tile value        
        int value = 0;
        if (t.getType().getPrimaryGoods() != null) {
            value += t.potential(t.getType().getPrimaryGoods().getType(), null) * PRIMARY_GOODS_VALUE;
        }
        //value += t.potential(t.secondaryGoods(), null) * t.secondaryGoods().getInitialSellPrice();

        //multiplicative modifier, to be applied to value later
        float advantage = 1f;

        //set up maps for all foods and building materials
        final Specification spec = getSpecification();
        TypeCountMap<GoodsType> rawBuildingMaterialMap
            = new TypeCountMap<GoodsType>();
        for (GoodsType g : spec.getRawBuildingGoodsTypeList()) {
            rawBuildingMaterialMap.incrementCount(g, 0);
        }
        TypeCountMap<GoodsType> foodMap = new TypeCountMap<GoodsType>();
        for (GoodsType g : spec.getFoodGoodsTypeList()) {
            foodMap.incrementCount(g, 0);
        }

        //penalty for building on a resource tile,
        //because production can not be improved much
        if (t.hasResource()) {
            advantage *= MOD_HAS_RESOURCE;
        }

        //penalty if path to europe doesn't exist, or is too long
        //TODO: instead check tile.isConnected() for neighbouring tiles,
        // should be more efficient?
        final PathNode n = getGame().getMap().findPathToEurope(t);
        if (n == null) {
            // no path to Europe, therefore it is a poor location
            // TODO: at the moment, this means we are land-locked
            advantage *= MOD_NO_PATH;
        } else if (n.getTotalTurns() > LONG_PATH_TURNS) {
            advantage *= MOD_LONG_PATH;
        }

        boolean supportingColony = false;
        Iterator<Position> it;
        for (int radius = 1; radius < 5; radius++) {
            it = getGame().getMap().getCircleIterator(t.getPosition(), false, radius);
            while (it.hasNext()) {
                Tile tile = getGame().getMap().getTile(it.next());
                Settlement set = tile.getSettlement(); //may be null!
                Colony col = tile.getColony(); //may be null!

                if (radius==1) {
                    //already checked: no colony here - if set!=null, it's indian
                    if (set != null) {
                        //penalize building next to native settlement
                        SettlementType type = set.getType();
                        if (type.getClaimableRadius() > 1) {
                            // really shouldn't build next to cities
                            advantage *= MOD_ADJ_SETTLEMENT_BIG;
                        } else {
                            advantage *= MOD_ADJ_SETTLEMENT;
                        }

                    //no settlement on neighbouring tile
                    } else {
                        //apply penalty for owned neighbouring tiles
                        if (tile.getOwner() != null && !this.owns(tile)) {
                            if (tile.getOwner().isEuropean()) {
                                advantage *= MOD_OWNED_EUROPEAN;
                            } else {
                                advantage *= MOD_OWNED_NATIVE;
                            }
                        }

                        //count production
                        if (tile.getType()!=null) {
                            for (AbstractGoods production : tile.getType().getProduction()) {
                                GoodsType type = production.getType();
                                int potential = tile.potential(type, null);
                                value += potential * type.getInitialSellPrice();
                                // a few tiles with high production are better
                                // than many tiles with low production
                                int highProductionValue = 0;
                                if (potential > HIGH_PRODUCTION) {
                                    advantage *= MOD_HIGH_PRODUCTION;
                                    highProductionValue = 2;
                                } else if (potential > GOOD_PRODUCTION) {
                                    advantage *= MOD_GOOD_PRODUCTION;
                                    highProductionValue = 1;
                                }
                                if (type.isFoodType()) {
                                    foodMap.incrementCount(type, highProductionValue);
                                } else if (type.isRawBuildingMaterial()) {
                                    rawBuildingMaterialMap.incrementCount(type, highProductionValue);
                                }
                            }
                        }
                    }

                //radius > 1
                } else {
                    if (value <= 0) {
                        //value no longer changes, so return if still <=0
                        return 0;
                    }
                    if (col != null) {
                        //apply modifier for own colony at this distance
                        if (col.getOwner()==this) {
                            if (!supportingColony) {
                                supportingColony = true;
                                advantage *= MOD_OWN_COLONY[radius];
                            }
                        //apply modifier for other colonies at this distance
                        } else {
                            if (atWarWith(col.getOwner())) {
                                advantage *= MOD_ENEMY_COLONY[radius];
                            } else {
                                advantage *= MOD_NEUTRAL_COLONY[radius];
                            }
                        }
                    }
                }

                Iterator<Unit> ui = tile.getUnitIterator();
                while (ui.hasNext()) {
                    Unit u = ui.next();
                    if (u.getOwner() != this && u.isOffensiveUnit()
                        && u.getOwner().isEuropean()
                        && atWarWith(u.getOwner())) {
                        advantage *= MOD_ENEMY_UNIT[radius];
                    }
                }
            }
        }

        //check availability of key goods        
        for (GoodsType type : rawBuildingMaterialMap.keySet()) {
            Integer amount = rawBuildingMaterialMap.getCount(type);
            if (amount == 0) {
                advantage *= MOD_BUILD_MATERIAL_MISSING;
            }
        }
        int foodProduction = 0;
        for (Integer food : foodMap.values()) {
            foodProduction += food;
        }
        if (foodProduction < FOOD_VERY_LOW) {
            advantage *= MOD_FOOD_VERY_LOW;
        } else if (foodProduction < FOOD_LOW) {
            advantage *= MOD_FOOD_LOW;
        }

        return (int) (value * advantage);
    }

    /**
     * Returns the stance towards a given player. <BR>
     * <BR>
     * One of: WAR, CEASE_FIRE, PEACE and ALLIANCE.
     *
     * @param player The <code>Player</code>.
     * @return The stance.
     */
    public Stance getStance(Player player) {
        return (player == null || stance.get(player.getId()) == null)
            ? Stance.UNCONTACTED
            : stance.get(player.getId());
    }

    /**
     * Sets the stance towards a given player to one of
     * WAR, CEASE_FIRE, PEACE and ALLIANCE.
     *
     * @param player The <code>Player</code>.
     * @param newStance The new <code>Stance</code>.
     * @return True if the stance change was valid.
     * @throws IllegalArgumentException if player is null or this.
     */
    public boolean setStance(Player player, Stance newStance) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be 'null'.");
        }
        if (player == this) {
            throw new IllegalArgumentException("Cannot set the stance towards ourselves.");
        }
        if (newStance == null) {
            stance.remove(player.getId());
            return true;
        }
        Stance oldStance = stance.get(player.getId());
        if (newStance.equals(oldStance)) return true;

        boolean valid = true;;
        if ((newStance == Stance.CEASE_FIRE && oldStance != Stance.WAR)
            || newStance == Stance.UNCONTACTED) {
            valid = false;
        }
        stance.put(player.getId(), newStance);
        return valid;
    }

    /**
     * Is this player at war with the specified one.
     *
     * @param player The <code>Player</code> to check.
     * @return True if the players are at war.
     */
    public boolean atWarWith(Player player) {
        return getStance(player) == Stance.WAR;
    }

    /**
     * Returns whether this player has met with the <code>Player</code> if the
     * given <code>nation</code>.
     *
     * @param player The Player.
     * @return <code>true</code> if this <code>Player</code> has contacted
     *         the given nation.
     */
    public boolean hasContacted(Player player) {
        return getStance(player) != Stance.UNCONTACTED;
    }

    /**
     * Returns whether this player has met with any Europeans at all.
     *
     * @return <code>true</code> if this <code>Player</code> has contacted
     *         any Europeans.
     */
    public boolean hasContactedEuropeans() {
        for (Player other : getGame().getLiveEuropeanPlayers()) {
            if (other != this && hasContacted(other)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns whether this player has met with any natives at all.
     *
     * @return <code>true</code> if this <code>Player</code> has contacted
     *         any natives.
     */
    public boolean hasContactedIndians() {
        for (Player other : getGame().getPlayers()) {
            if (other != this && !other.isDead() && other.isIndian()
                && hasContacted(other)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set this player as having made initial contact with another player.
     * Always start with PEACE, which can go downhill fast.
     *
     * @param player1 a <code>Player</code> value
     * @param player2 a <code>Player</code> value
     */
    public static void makeContact(Player player1, Player player2) {
        player1.stance.put(player2.getId(), Stance.PEACE);
        player2.stance.put(player1.getId(), Stance.PEACE);
        player1.setTension(player2, new Tension(Tension.TENSION_MIN));
        player2.setTension(player1, new Tension(Tension.TENSION_MIN));
    }

    /**
     * Has a player visited a native settlement?
     *
     * This is needed by the native report and settlement popup to
     * clarify whether the skill at the settlement is null because it
     * has been depleted, or because we just have not found out what
     * it is yet.
     *
     * @param settlement The <code>IndianSettlement</code> to query.
     * @return True if the player has visited the settlement.
     */
    public boolean hasVisited(IndianSettlement settlement) {
        return settlement.hasContactedSettlement(this);
    }

    /**
     * Gets the price for a recruit in europe.
     *
     * @return The price of a single recruit in {@link Europe}.
     */
    public int getRecruitPrice() {
        // return Math.max(0, (getCrossesRequired() - crosses) * 10);
        return getEurope().getRecruitPrice();
    }

    /**
     * Gets the current amount of liberty this <code>Player</code> has.
     *
     * @return This player's number of liberty earned towards the
     *     current Founding Father.
     */
    public int getLiberty() {
        return (canHaveFoundingFathers()) ? liberty : 0;
    }

    /**
     * Sets the current amount of liberty this player has.
     *
     * @param liberty The new amount of liberty.
     */
    public void setLiberty(int liberty) {
        this.liberty = liberty;
    }

    /**
     * Adds to the current amount of liberty this player has.
     *
     * @param amount The additional amount of liberty.
     */
    public void incrementLiberty(int amount) {
        setLiberty(Math.max(0, getLiberty() + amount));
    }

    /**
     * Returns how many total liberty will be produced if no colonies
     * are lost and nothing unexpected happens.
     *
     * @return Total number of liberty this <code>Player</code>'s
     *         <code>Colony</code>s will make.
     * @see #incrementLiberty
     */
    public int getLibertyProductionNextTurn() {
        int libertyNextTurn = 0;
        for (Colony colony : getColonies()) {
            for (GoodsType libertyGoods : getSpecification()
                     .getLibertyGoodsTypeList()) {
                libertyNextTurn += colony.getProductionOf(libertyGoods);
            }
        }
        return libertyNextTurn;
    }

    /**
     * Reset the player iterators ready for a new turn.
     */
    public void resetIterators() {
        nextActiveUnitIterator.reset();
        nextGoingToUnitIterator.reset();
    }

    /**
     * Returns the arrears due for a type of goods.
     *
     * @param type a <code>GoodsType</code> value
     * @return The arrears due for this type of goods.
     */
    public int getArrears(GoodsType type) {
        return getMarket().getArrears(type);
    }

    /**
     * Returns the arrears due for a type of goods.
     *
     * @param goods The goods.
     * @return The arrears due for this type of goods.
     */
    public int getArrears(Goods goods) {
        return getArrears(goods.getType());
    }

    /**
     * Returns true if type of goods can be traded in Europe.
     *
     * @param type The goods type.
     * @return True if there are no arrears due for this type of goods.
     */
    public boolean canTrade(GoodsType type) {
        return canTrade(type, Market.Access.EUROPE);
    }

    /**
     * Returns true if type of goods can be traded at specified place.
     *
     * @param type The GoodsType.
     * @param access The way the goods are traded (Europe OR Custom)
     * @return <code>true</code> if type of goods can be traded.
     */
    public boolean canTrade(GoodsType type, Market.Access access) {
        if (getMarket().getArrears(type) == 0) {
            return true;
        }
        if (access == Market.Access.CUSTOM_HOUSE) {
            if (getSpecification().getBoolean(GameOptions.CUSTOM_IGNORE_BOYCOTT)) {
                return true;
            }
            if (hasAbility("model.ability.customHouseTradesWithForeignCountries")) {
                for (Player otherPlayer : getGame().getLiveEuropeanPlayers()) {
                    if (otherPlayer != this
                        && (getStance(otherPlayer) == Stance.PEACE
                            || getStance(otherPlayer) == Stance.ALLIANCE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if type of goods can be traded at specified place
     *
     * @param goods The goods.
     * @param access Place where the goods are traded (Europe OR Custom)
     * @return True if type of goods can be traded.
     */
    public boolean canTrade(Goods goods, Market.Access access) {
        return canTrade(goods.getType(), access);
    }

    /**
     * Returns true if type of goods can be traded in Europe.
     *
     * @param goods The goods.
     * @return True if there are no arrears due for this type of goods.
     */
    public boolean canTrade(Goods goods) {
        return canTrade(goods, Market.Access.EUROPE);
    }

    /**
     * Returns the current tax.
     *
     * @return The current tax.
     */
    public int getTax() {
        return tax;
    }

    /**
     * Sets the current tax
     *
     * @param amount The new tax.
     */
    public void setTax(int amount) {
        tax = amount;
        if (recalculateBellsBonus()) {
            for (Colony colony : getColonies()) {
                colony.invalidateCache();
            }
        }
    }

    /**
     * Recalculate bells bonus when tax changes.
     *
     * @return True if a bells bonus was set.
     */
    protected boolean recalculateBellsBonus() {
        Set<Modifier> libertyBonus
            = featureContainer.getModifierSet("model.goods.bells");
        boolean ret = false;
        for (Ability ability : featureContainer.getAbilitySet("model.ability.addTaxToBells")) {
            FreeColGameObjectType source = ability.getSource();
            if (source != null) {
                for (Modifier modifier : libertyBonus) {
                    if (source.equals(modifier.getSource())) {
                        modifier.setValue(tax);
                        ret = true;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Returns the current sales.
     *
     * @param goodsType a <code>GoodsType</code> value
     * @return The current sales.
     */
    public int getSales(GoodsType goodsType) {
        return getMarket().getSales(goodsType);
    }

    /**
     * Modifies the current sales.
     *
     * @param goodsType a <code>GoodsType</code> value
     * @param amount The new sales.
     */
    public void modifySales(GoodsType goodsType, int amount) {
        getMarket().modifySales(goodsType, amount);
    }

    /**
     * Has a type of goods been traded?
     *
     * @param goodsType a <code>GoodsType</code> value
     * @return Whether these goods have been traded.
     */
    public boolean hasTraded(GoodsType goodsType) {
        return getMarket().hasBeenTraded(goodsType);
    }

    /**
     * Returns the most valuable goods available in one of the
     * player's colonies for the purposes of choosing a
     * threat-to-boycott.  The goods must not currently be boycotted,
     * the player must have traded in it, and the amount to be discarded
     * will not exceed GoodsContainer.CARGO_SIZE.
     *
     * @return A goods object, or null.
     */
    public Goods getMostValuableGoods() {
        if (!isEuropean()) return null;

        Goods goods = null;
        int highValue = 0;
        for (Colony colony : getColonies()) {
            for (Goods g : colony.getCompactGoods()) {
                if (getArrears(g.getType()) <= 0 && hasTraded(g.getType())) {
                    int amount = Math.min(g.getAmount(),
                                          GoodsContainer.CARGO_SIZE);
                    int value = market.getSalePrice(g.getType(), amount);
                    if (value > highValue) {
                        highValue = value;
                        goods = g;
                    }
                }
            }
        }
        return goods;
    }

    /**
     * Returns the current incomeBeforeTaxes.
     *
     * @param goodsType The GoodsType.
     * @return The current incomeBeforeTaxes.
     */
    public int getIncomeBeforeTaxes(GoodsType goodsType) {
        return getMarket().getIncomeBeforeTaxes(goodsType);
    }

    /**
     * Modifies the current incomeBeforeTaxes.
     *
     * @param goodsType The GoodsType.
     * @param amount The new incomeBeforeTaxes.
     */
    public void modifyIncomeBeforeTaxes(GoodsType goodsType, int amount) {
        getMarket().modifyIncomeBeforeTaxes(goodsType, amount);
    }

    /**
     * Returns the current incomeAfterTaxes.
     *
     * @param goodsType The GoodsType.
     * @return The current incomeAfterTaxes.
     */
    public int getIncomeAfterTaxes(GoodsType goodsType) {
        return getMarket().getIncomeAfterTaxes(goodsType);
    }

    /**
     * Modifies the current incomeAfterTaxes.
     *
     * @param goodsType The GoodsType.
     * @param amount The new incomeAfterTaxes.
     */
    public void modifyIncomeAfterTaxes(GoodsType goodsType, int amount) {
        getMarket().modifyIncomeAfterTaxes(goodsType, amount);
    }

    /**
     * Add a HistoryEvent to this player.
     *
     * @param event The <code>HistoryEvent</code> to add.
     */
    public void addHistory(HistoryEvent event) {
        history.add(event);
    }

    /**
     * Checks if the given <code>Player</code> equals this object.
     *
     * @param o The <code>Player</code> to compare against this object.
     * @return <i>true</i> if the two <code>Player</code> are equal and none
     *         of both have <code>nation == null</code> and <i>false</i>
     *         otherwise.
     */
    public boolean equals(Player o) {
        if (o == null) {
            return false;
        } else if (getId() == null || o.getId() == null) {
            // This only happens in the client code with the virtual "enemy
            // privateer" player
            // This special player is not properly associated to the Game and
            // therefore has no ID
            // TODO: remove this hack when the virtual "enemy privateer" player
            // is better implemented
            return false;
        } else {
            return getId().equals(o.getId());
        }
    }


    /**
     * Gets the name index for a given key.
     *
     * @param key The key to use.
     */
    public int getNameIndex(String key) {
        Integer val = nameIndex.get(key);
        return (val == null) ? 0 : val;
    }

    /**
     * Gets the name index for a given key.
     *
     * @param key The key to use.
     */
    public void setNameIndex(String key, int value) {
        nameIndex.put(key, new Integer(value));
    }

    /**
     * A predicate that can be applied to a unit.
     */
    public abstract class UnitPredicate {
        public abstract boolean obtains(Unit unit);
    }

    /**
     * A predicate for determining active units.
     */
    public class ActivePredicate extends UnitPredicate {

        private final Player player;

        public ActivePredicate(Player player) {
            this.player = player;
        }

        /**
         * Returns true if the unit is active (and going nowhere).
         */
        public boolean obtains(Unit unit) {
            return !unit.isDisposed()
                && unit.getOwner() == player
                && unit.getMovesLeft() > 0
                && unit.getState() == Unit.UnitState.ACTIVE
                && unit.getDestination() == null
                && unit.getTradeRoute() == null
                && !(unit.getLocation() instanceof WorkLocation)
                && unit.getTile() != null;
        }
    }

    /**
     * A predicate for determining units going somewhere.
     */
    public class GoingToPredicate extends UnitPredicate {

        private final Player player;

        public GoingToPredicate(Player player) {
            this.player = player;
        }

        /**
         * Returns true if the unit has order to go somewhere.
         */
        public boolean obtains(Unit unit) {
            return !unit.isDisposed()
                && unit.getOwner() == player
                && unit.getMovesLeft() > 0
                && unit.getState() != Unit.UnitState.SKIPPED
                && (unit.getDestination() != null
                    || unit.getTradeRoute() != null)
                && !(unit.getLocation() instanceof WorkLocation)
                && unit.getTile() != null;
        }
    }

    /**
     * Saves a LastSale record.
     *
     * @param sale The <code>LastSale</code> to save.
     */
    public void saveSale(LastSale sale) {
        if (lastSales == null) lastSales = new HashMap<String, LastSale>();
        lastSales.put(sale.getId(), sale);
    }

    /**
     * Gets the current sales data for a location and goods type.
     *
     * @param where The <code>Location</code> of the sale.
     * @param what The <code>GoodsType</code> sold.
     *
     * @return An appropriate <code>LastSaleData</code> record or null.
     */
    public LastSale getLastSale(Location where, GoodsType what) {
        return (lastSales == null) ? null
            : lastSales.get(LastSale.makeKey(where, what));
    }

    /**
     * Gets the last sale price for a location and goods type as a string.
     *
     * @param where The <code>Location</code> of the sale.
     * @param what The <code>GoodsType</code> sold.
     * @return An abbreviation for the sale price, or null if none found.
     */
    public String getLastSaleString(Location where, GoodsType what) {
        LastSale data = getLastSale(where, what);
        return (data == null) ? null : String.valueOf(data.getPrice());
    }


    /**
     * An <code>Iterator</code> of {@link Unit}s that can be made active.
     */
    public class UnitIterator implements Iterator<Unit> {

        private Player owner;

        private UnitPredicate predicate;

        private List<Unit> units = null;

        /**
         * A comparator to compare units by position, top to bottom,
         * left to right.
         */
        private final Comparator<Unit> xyComparator = new Comparator<Unit>() {
            public int compare(Unit unit1, Unit unit2) {
                Tile tile1 = unit1.getTile();
                Tile tile2 = unit2.getTile();
                int cmp = ((tile1 == null) ? 0 : tile1.getY())
                    - ((tile2 == null) ? 0 : tile2.getY());
                return (cmp != 0 || tile1 == null || tile2 == null) ? cmp
                    : (tile1.getX() - tile2.getX());
            }
        };

        /**
         * Creates a new <code>UnitIterator</code>.
         *
         * @param owner The <code>Player</code> that needs an iterator of it's
         *            units.
         * @param predicate An object for deciding whether a <code>Unit</code>
         *            should be included in the <code>Iterator</code> or not.
         */
        public UnitIterator(Player owner, UnitPredicate predicate) {
            this.owner = owner;
            this.predicate = predicate;
            reset();
        }

        /**
         * Reset the internal units list, initially only with units that
         * satisfy the predicate.
         */
        public void reset() {
            units = new ArrayList<Unit>();
            for (Unit u : owner.getUnits()) {
                if (predicate.obtains(u)) units.add(u);
            }
            Collections.sort(units, xyComparator);
        }

        /**
         * Check if there is any more valid units.
         * If there are, it will be at the head of the internal units list.
         *
         * @return True if there are any valid units left.
         */
        public boolean hasNext() {
            // Try to find a unit that still satisfies the predicate.
            while (!units.isEmpty()) {
                if (predicate.obtains(units.get(0))) {
                    return true; // Still valid
                }
                units.remove(0);
            }
            // Nothing left, so refill the units list.  If it is still
            // empty then there is definitely nothing left.
            reset();
            return !units.isEmpty();
        }

        /**
         * Get the next valid unit.
         * Always call hasNext to enforce validity.
         *
         * @return The next valid unit, or null if none.
         */
        public Unit next() {
            return (hasNext()) ? units.remove(0) : null;
        }

        /**
         * Set the next valid unit.
         *
         * @param unit The <code>Unit</code> to put at the front of the list.
         * @return True if the operation succeeds.
         */
        public boolean setNext(Unit unit) {
            if (predicate.obtains(unit)) { // Of course, it has to be valid...
                Unit first = (units.isEmpty()) ? null : units.get(0);
                while (!units.isEmpty()) {
                    if (units.get(0) == unit) return true;
                    units.remove(0);
                }
                reset();
                while (!units.isEmpty() && units.get(0) != first) {
                    if (units.get(0) == unit) return true;
                    units.remove(0);
                }
            }
            return false;
        }

        /**
         * Removes from the underlying collection the last element returned by
         * the iterator (optional operation).
         *
         * @exception UnsupportedOperationException no matter what.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    /**
     * This method writes an XML-representation of this object to the given
     * stream. <br>
     * <br>
     * Only attributes visible to the given <code>Player</code> will be added
     * to that representation if <code>showAll</code> is set to
     * <code>false</code>.
     *
     * @param out The target stream.
     * @param player The <code>Player</code> this XML-representation should be
     *            made for, or <code>null</code> if
     *            <code>showAll == true</code>.
     * @param showAll Only attributes visible to <code>player</code> will be
     *            added to the representation if <code>showAll</code> is set
     *            to <i>false</i>.
     * @param toSavedGame If <code>true</code> then information that is only
     *            needed when saving a game is added.
     * @throws XMLStreamException if there are any problems writing to the
     *             stream.
     */
    protected void toXMLImpl(XMLStreamWriter out, Player player,
                             boolean showAll, boolean toSavedGame)
        throws XMLStreamException {
        // Start element:
        out.writeStartElement(getXMLElementTagName());

        writeAttributes(out, player, showAll, toSavedGame);
        writeChildren(out, player, showAll, toSavedGame);

        out.writeEndElement();
    }


    protected void writeAttributes(XMLStreamWriter out, Player player,
                                   boolean showAll, boolean toSavedGame)
        throws XMLStreamException {

        out.writeAttribute(ID_ATTRIBUTE, getId());
        out.writeAttribute("username", name);
        out.writeAttribute("nationID", nationID);
        if (nationType != null) {
            out.writeAttribute("nationType", nationType.getId());
        }
        out.writeAttribute("admin", Boolean.toString(admin));
        out.writeAttribute("ready", Boolean.toString(ready));
        out.writeAttribute("dead", Boolean.toString(dead));
        out.writeAttribute("playerType", playerType.toString());
        out.writeAttribute("ai", Boolean.toString(ai));
        out.writeAttribute("tax", Integer.toString(tax));

        // @compat 0.9.x
        out.writeAttribute("numberOfSettlements", Integer.toString(getNumberOfSettlements()));
        // end compatibility code

        if (showAll || toSavedGame || equals(player)) {
            out.writeAttribute("gold", Integer.toString(gold));
            out.writeAttribute("immigration", Integer.toString(immigration));
            out.writeAttribute("liberty", Integer.toString(liberty));
            if (currentFather != null) {
                out.writeAttribute("currentFather", currentFather.getId());
            }
            out.writeAttribute("immigrationRequired", Integer.toString(immigrationRequired));
            out.writeAttribute("attackedByPrivateers", Boolean.toString(attackedByPrivateers));
            out.writeAttribute("oldSoL", Integer.toString(oldSoL));
            out.writeAttribute("score", Integer.toString(score));
        } else {
            out.writeAttribute("gold", Integer.toString(-1));
            out.writeAttribute("immigration", Integer.toString(-1));
            out.writeAttribute("liberty", Integer.toString(-1));
            out.writeAttribute("immigrationRequired", Integer.toString(-1));
        }
        if (newLandName != null) {
            out.writeAttribute("newLandName", newLandName);
        }
        if (independentNationName != null) {
            out.writeAttribute("independentNationName", independentNationName);
        }
        if (entryLocation != null) {
            out.writeAttribute("entryLocation", entryLocation.getId());
        }
        for (RegionType regionType : RegionType.values()) {
            String key = regionType.getNameIndexKey();
            int index = getNameIndex(key);
            if (index > 0) out.writeAttribute(key, Integer.toString(index));
        }
    }

    protected void writeChildren(XMLStreamWriter out, Player player,
                                 boolean showAll, boolean toSavedGame)
        throws XMLStreamException {

        for (Entry<Player, Tension> entry : tension.entrySet()) {
            out.writeStartElement(TENSION_TAG);
            out.writeAttribute("player", entry.getKey().getId());
            out.writeAttribute(VALUE_TAG, String.valueOf(entry.getValue().getValue()));
            out.writeEndElement();
        }

        for (Entry<String, Stance> entry : stance.entrySet()) {
            out.writeStartElement(STANCE_TAG);
            out.writeAttribute("player", entry.getKey());
            out.writeAttribute(VALUE_TAG, entry.getValue().toString());
            out.writeEndElement();
        }

        for (HistoryEvent event : history) {
            event.toXML(out);
        }

        for (TradeRoute route : tradeRoutes) {
            route.toXML(out, this, false, false);
        }

        if (market != null) {
            market.toXML(out, player, showAll, toSavedGame);
        }

        if (highSeas != null) {
            highSeas.toXMLImpl(out, player, showAll, toSavedGame);
        }

        if (showAll || toSavedGame || equals(player)) {
            out.writeStartElement(FOUNDING_FATHER_TAG);
            out.writeAttribute(ARRAY_SIZE, Integer.toString(allFathers.size()));
            int index = 0;
            for (FoundingFather father : allFathers) {
                out.writeAttribute("x" + Integer.toString(index), father.getId());
                index++;
            }
            out.writeEndElement();

            out.writeStartElement(OFFERED_FATHER_TAG);
            out.writeAttribute(ARRAY_SIZE, Integer.toString(offeredFathers.size()));
            index = 0;
            for (FoundingFather father : offeredFathers) {
                out.writeAttribute("x" + Integer.toString(index), father.getId());
                index++;
            }
            out.writeEndElement();

            if (europe != null) {
                europe.toXML(out, player, showAll, toSavedGame);
            }
            if (monarch != null) {
                monarch.toXML(out, player, showAll, toSavedGame);
            }
            if (!modelMessages.isEmpty()) {
                for (ModelMessage m : modelMessages) {
                    m.toXML(out);
                }
            }
            if (lastSales != null) {
                for (LastSale sale : lastSales.values()) {
                    sale.toXMLImpl(out);
                }
            }
            if (market != null) {
                market.toXML(out, player, showAll, toSavedGame);
            }
        }

    }

    /**
     * Initialize this object from an XML-representation of this object.
     *
     * @param in The input stream with the XML.
     */
    protected void readFromXMLImpl(XMLStreamReader in)
        throws XMLStreamException {
        setId(in.getAttributeValue(null, ID_ATTRIBUTE));
        name = in.getAttributeValue(null, "username");
        nationID = in.getAttributeValue(null, "nationID");
        if (!isUnknownEnemy()) {
            nationType = getSpecification().getNationType(in.getAttributeValue(null, "nationType"));
        }
        admin = getAttribute(in, "admin", false);
        gold = Integer.parseInt(in.getAttributeValue(null, "gold"));
        immigration = getAttribute(in, "immigration", 0);
        liberty = getAttribute(in, "liberty", 0);
        oldSoL = getAttribute(in, "oldSoL", 0);
        score = getAttribute(in, "score", 0);
        ready = getAttribute(in, "ready", false);
        ai = getAttribute(in, "ai", false);
        dead = getAttribute(in, "dead", false);
        tax = Integer.parseInt(in.getAttributeValue(null, "tax"));
        playerType = Enum.valueOf(PlayerType.class, in.getAttributeValue(null, "playerType"));
        currentFather = getSpecification().getType(in, "currentFather", FoundingFather.class, null);
        immigrationRequired = getAttribute(in, "immigrationRequired", 12);
        newLandName = getAttribute(in, "newLandName", null);
        independentNationName = getAttribute(in, "independentNationName", null);

        attackedByPrivateers = getAttribute(in, "attackedByPrivateers", false);
        final String entryLocationStr = in.getAttributeValue(null, "entryLocation");
        if (entryLocationStr != null) {
            entryLocation = (Location) getGame().getFreeColGameObject(entryLocationStr);
            if (entryLocation == null) {
                entryLocation = new Tile(getGame(), entryLocationStr);
            }
        }
        for (RegionType regionType : RegionType.values()) {
            String key = regionType.getNameIndexKey();
            int index = getAttribute(in, key, -1);
            if (index > 0) setNameIndex(key, index);
        }

        featureContainer = new FeatureContainer();
        if (nationType != null) {
            featureContainer.add(nationType.getFeatureContainer());
        }
        switch (playerType) {
        case REBEL:
        case INDEPENDENT:
            featureContainer.addAbility(new Ability("model.ability.independenceDeclared"));
            break;
        default:
            // no special abilities for other playertypes, but silent warning about unused enum.
            break;
        }

        tension.clear();
        stance.clear();
        allFathers.clear();
        offeredFathers.clear();
        europe = null;
        monarch = null;
        history.clear();
        tradeRoutes.clear();
        modelMessages.clear();
        lastSales = null;
        highSeas = null;
        while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
            if (in.getLocalName().equals(TENSION_TAG)) {
                Player player = (Player) getGame().getFreeColGameObject(in.getAttributeValue(null, "player"));
                tension.put(player, new Tension(getAttribute(in, VALUE_TAG, 0)));
                in.nextTag(); // close element
            } else if (in.getLocalName().equals(FOUNDING_FATHER_TAG)) {
                int length = Integer.parseInt(in.getAttributeValue(null, ARRAY_SIZE));
                for (int index = 0; index < length; index++) {
                    String fatherId = in.getAttributeValue(null, "x" + String.valueOf(index));
                    FoundingFather father = getSpecification().getFoundingFather(fatherId);
                    addFather(father);
                }
                in.nextTag();
            } else if (in.getLocalName().equals(OFFERED_FATHER_TAG)) {
                int length = Integer.parseInt(in.getAttributeValue(null, ARRAY_SIZE));
                for (int index = 0; index < length; index++) {
                    String fatherId = in.getAttributeValue(null, "x" + String.valueOf(index));
                    FoundingFather father = getSpecification().getFoundingFather(fatherId);
                    offeredFathers.add(father);
                }
                in.nextTag();
            } else if (in.getLocalName().equals(STANCE_TAG)) {
                String playerId = in.getAttributeValue(null, "player");
                stance.put(playerId, Enum.valueOf(Stance.class, in.getAttributeValue(null, VALUE_TAG)));
                in.nextTag(); // close element
            } else if (in.getLocalName().equals(HighSeas.getXMLElementTagName())) {
                highSeas = updateFreeColGameObject(in, HighSeas.class);
            } else if (in.getLocalName().equals(Europe.getXMLElementTagName())) {
                europe = updateFreeColGameObject(in, Europe.class);
            } else if (in.getLocalName().equals(Monarch.getXMLElementTagName())) {
                monarch = updateFreeColGameObject(in, Monarch.class);
            } else if (in.getLocalName().equals(HistoryEvent.getXMLElementTagName())) {
                HistoryEvent event = new HistoryEvent();
                event.readFromXMLImpl(in);
                getHistory().add(event);
            } else if (in.getLocalName().equals(TradeRoute.getXMLElementTagName())) {
                TradeRoute route = updateFreeColGameObject(in, TradeRoute.class);
                tradeRoutes.add(route);
            } else if (in.getLocalName().equals(Market.getXMLElementTagName())) {
                market = updateFreeColGameObject(in, Market.class);
            } else if (in.getLocalName().equals(ModelMessage.getXMLElementTagName())) {

                ModelMessage message = new ModelMessage();
                message.readFromXMLImpl(in);
                addModelMessage(message);
            } else if (in.getLocalName().equals(LastSale.getXMLElementTagName())) {
                LastSale lastSale = new LastSale();
                lastSale.readFromXMLImpl(in);
                saveSale(lastSale);
            } else {
                logger.warning("Unknown tag: " + in.getLocalName() + " loading player");
                in.nextTag();
            }
        }

        // sanity check: we should be on the closing tag
        if (!in.getLocalName().equals(Player.getXMLElementTagName())) {
            logger.warning("Error parsing xml: expecting closing tag </" + Player.getXMLElementTagName() + "> "
                           + "found instead: " + in.getLocalName());
        }

        // TODO: This should no longer happen.  Remove soon (early 2012)
        // if further testing never triggers the following warning.
        if (market == null) {
            logger.warning("Null market for " + getName());
            Thread.dumpStack();
            market = new Market(getGame(), this);
        }

        // Bells bonuses depend on tax
        recalculateBellsBonus();

        invalidateCanSeeTiles();
    }

    /**
     * Partial writer for players, so that simple updates to fields such
     * as gold can be brief.
     *
     * @param out The target stream.
     * @param fields The fields to write.
     * @throws XMLStreamException If there are problems writing the stream.
     */
    @Override
    protected void toXMLPartialImpl(XMLStreamWriter out, String[] fields)
        throws XMLStreamException {
        toXMLPartialByClass(out, getClass(), fields);
    }

    /**
     * Partial reader for players, so that simple updates to fields such
     * as gold can be brief.
     *
     * @param in The input stream with the XML.
     * @throws XMLStreamException If there are problems reading the stream.
     */
    @Override
    protected void readFromXMLPartialImpl(XMLStreamReader in)
        throws XMLStreamException {
        readFromXMLPartialByClass(in, getClass());
    }

    /**
     * Gets the tag name of the root element representing this object.
     *
     * @return "player"
     */
    public static String getXMLElementTagName() {
        return "player";
    }
}

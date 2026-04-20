package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum AnimalData implements IMobConfig {

    // --- Bébés (Lv1-10) ---
    BISON_CALF("bison calf", 25, 1, 10),
    CAMEL_CALF("camel calf", 25, 1, 10),
    HORSE_FOAL("horse foal", 20, 1, 10),
    COW_CALF("cow calf", 20, 1, 10),
    PIG_WILD_PIGLET("pig wild piglet", 15, 1, 10),
    PIG_PIGLET("pig piglet", 12, 1, 10),
    BOAR_PIGLET("boar piglet", 18, 1, 10),
    SHEEP_LAMB("sheep lamb", 15, 1, 10),
    RAM_LAMB("ram lamb", 20, 1, 10),
    MOUFLON_LAMB("mouflon lamb", 18, 1, 10),
    GOAT_KID("goat kid", 15, 1, 10),
    WARTHOG_PIGLET("warthog piglet", 22, 1, 10),
    TURKEY_CHICK("turkey chick", 8, 1, 10),
    SKRILL_CHICK("skrill chick", 8, 1, 10),
    CHICKEN_DESERT_CHICK("chicken desert chick", 5, 1, 10),
    CHICKEN_CHICK("chicken chick", 5, 1, 10),

    // --- Petits critters & oiseaux (Lv1-25) ---
    SWARM_BEES("swarm bees", 12, 1, 15),
    VULTURE("vulture", 25, 5, 25),
    HAWK("hawk", 20, 5, 25),
    OWL_SNOW("owl snow", 15, 5, 20),
    OWL_BROWN("owl brown", 15, 5, 20),
    RAVEN("raven", 15, 1, 20),
    RABBIT("rabbit", 8, 1, 15),
    CROW("crow", 8, 1, 15),
    PARROT("parrot", 10, 1, 15),
    FLAMINGO("flamingo", 18, 1, 20),
    PENGUIN("penguin", 12, 1, 20),
    DUCK("duck", 8, 1, 15),
    CHICKEN_DESERT("chicken desert", 12, 1, 15),
    CHICKEN_BASE("chicken", 10, 1, 15),
    BLUEBIRD("bluebird", 5, 1, 10),
    FINCH_GREEN("finch green", 5, 1, 10),
    SPARROW("sparrow", 5, 1, 10),
    PIGEON("pigeon", 5, 1, 10),
    WOODPECKER("woodpecker", 5, 1, 10),
    FROG_BLUE("frog blue", 8, 1, 15),
    FROG_GREEN("frog green", 8, 1, 15),
    FROG_ORANGE("frog orange", 8, 1, 15),
    GECKO("gecko", 8, 1, 15),
    ARMADILLO("armadillo", 25, 1, 25),
    MEERKAT("meerkat", 12, 1, 15),
    SQUIRREL("squirrel", 5, 1, 10),
    MOUSE("mouse", 5, 1, 10),
    BAT_ICE("bat ice", 15, 1, 20),
    BAT("bat", 10, 1, 15),
    TORTOISE("tortoise", 25, 1, 25),
    HATWORM("hatworm", 15, 1, 20),
    BUNNY("bunny", 8, 1, 15),
    TETRABIRD("tetrabird", 12, 1, 20),

    // --- Bétail adulte (Lv1-30) ---
    BISON("bison", 70, 5, 30),
    CAMEL("camel", 65, 5, 30),
    HORSE("horse", 60, 5, 30),
    COW_BASE("cow", 50, 1, 30),
    PIG_WILD("pig wild", 50, 5, 30),
    PIG_BASE("pig", 35, 1, 25),
    BOAR("boar", 60, 5, 30),
    SHEEP_BASE("sheep", 40, 1, 25),
    RAM("ram", 60, 5, 30),
    MOUFLON("mouflon", 50, 5, 30),
    GOAT("goat", 45, 1, 25),
    WARTHOG("warthog", 65, 10, 30),
    ANTELOPE("antelope", 50, 1, 30),
    DEER_STAG("deer stag", 60, 5, 30),
    DEER_DOE("deer doe", 45, 1, 25),
    TURKEY("turkey", 30, 1, 25),
    SKRILL("skrill", 30, 1, 25),
    DOG("dog", 40, 1, 30),
    KITTEN("kitten", 25, 1, 20),

    // --- Petits prédateurs / nuisibles (Lv20-70) ---
    FOX("fox", 35, 20, 60),
    RAT("rat", 8, 1, 15),
    LARVA_SILK("larva silk", 15, 1, 15),
    MOLERAT("molerat", 30, 20, 60),
    SLUG_MAGMA("slug magma", 50, 30, 70),
    SNAIL_MAGMA("snail magma", 40, 25, 60),
    SNAIL_FROST("snail frost", 40, 25, 60),
    CACTEE("cactee", 35, 20, 60),
    HYENA("hyena", 55, 30, 70),
    SNAKE_RATTLE("snake rattle", 45, 25, 65),
    SNAKE_MARSH("snake marsh", 40, 20, 60),

    // --- Prédateurs moyens (Lv30-100) ---
    WOLF_BLACK("wolf black", 80, 30, 90),
    WOLF_WHITE("wolf white", 80, 30, 90),
    LEOPARD_SNOW("leopard snow", 100, 40, 100),
    LIZARD_SAND("lizard sand", 75, 30, 90),
    SCORPION("scorpion", 100, 40, 100),
    CROCODILE("crocodile", 130, 50, 100),
    SPIDER_CAVE("spider cave", 80, 35, 90),
    SPIDER_BASE("spider", 60, 25, 80),
    SNAKE_COBRA("snake cobra", 70, 30, 90),

    // --- Gros prédateurs (Lv60-150) ---
    TIGER_SABERTOOTH("tiger sabertooth", 220, 70, 150),
    BEAR_GRIZZLY("bear grizzly", 200, 60, 140),
    BEAR_POLAR("bear polar", 180, 60, 140),
    MOOSE_BULL("moose bull", 200, 70, 140),
    MOOSE_COW("moose cow", 150, 60, 120),
    TOAD_RHINO_MAGMA("toad rhino magma", 220, 80, 150),
    TOAD_RHINO_GREEN("toad rhino green", 200, 70, 140),
    TOAD_RHINO("toad rhino", 180, 60, 130),
    MOSSHORN_PLAIN("mosshorn plain", 200, 70, 140),
    MOSSHORN("mosshorn", 180, 60, 130),

    // --- Morts-vivants (Lv20-150) ---
    HORSE_SKELETON_ARMORED("horse skeleton armored", 250, 80, 150),
    HORSE_SKELETON("horse skeleton", 150, 50, 120),
    COW_UNDEAD("cow undead", 90, 40, 90),
    PIG_UNDEAD("pig undead", 60, 30, 70),
    CHICKEN_UNDEAD("chicken undead", 40, 20, 50);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    AnimalData(String keyword, int xp, int minLevel, int maxLevel) {
        this.keyword = keyword;
        this.xp = xp;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.isInvincible = false;
        this.customTitle = null;
    }

    @Override public String getKeyword() { return keyword; }
    @Override public int getXp() { return xp; }
    @Override public int getMinLevel() { return minLevel; }
    @Override public int getMaxLevel() { return maxLevel; }
    @Override public boolean isInvincible() { return isInvincible; }
    @Override public String getCustomTitle() { return customTitle; }
}

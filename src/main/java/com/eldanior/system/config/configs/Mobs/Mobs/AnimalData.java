package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum AnimalData implements IMobConfig {

    // --- Bétail & animaux de ferme (Lv1-50) ---
    BISON_CALF("bison calf", 50, 1, 50),
    BISON("bison", 90, 1, 50),
    SWARM_BEES("swarm bees", 10, 1, 5),
    CAMEL_CALF("camel calf", 50, 1, 50),
    CAMEL("camel", 90, 1, 50),
    HORSE_FOAL("horse foal", 30, 1, 40),
    HORSE("horse", 80, 1, 50),
    COW_CALF("cow calf", 30, 1, 40),
    COW_BASE("cow", 70, 1, 50),
    PIG_WILD_PIGLET("pig wild piglet", 20, 1, 40),
    PIG_WILD("pig wild", 50, 1, 50),
    PIG_PIGLET("pig piglet", 15, 1, 30),
    PIG_BASE("pig", 30, 1, 40),
    BOAR_PIGLET("boar piglet", 20, 1, 40),
    BOAR("boar", 60, 1, 50),
    SHEEP_LAMB("sheep lamb", 20, 1, 40),
    SHEEP_BASE("sheep", 50, 1, 50),
    DOG("dog", 50, 1, 50),
    KITTEN("kitten", 50, 1, 50),
    RAM_LAMB("ram lamb", 40, 1, 50),
    RAM("ram", 80, 1, 50),
    MOUFLON_LAMB("mouflon lamb", 30, 1, 40),
    MOUFLON("mouflon", 60, 1, 50),
    GOAT_KID("goat kid", 25, 1, 40),
    GOAT("goat", 60, 1, 50),
    WARTHOG_PIGLET("warthog piglet", 50, 1, 50),
    WARTHOG("warthog", 70, 1, 50),
    ANTELOPE("antelope", 60, 1, 50),
    DEER_STAG("deer stag", 70, 1, 50),
    DEER_DOE("deer doe", 50, 1, 50),
    TURKEY_CHICK("turkey chick", 10, 1, 30),
    TURKEY("turkey", 30, 1, 40),
    SKRILL_CHICK("skrill chick", 10, 1, 30),
    SKRILL("skrill", 20, 1, 40),
    // --- Prédateurs forts (Lv50-150) ---
    TIGER_SABERTOOTH("tiger sabertooth", 100, 50, 150),
    BEAR_GRIZZLY("bear grizzly", 100, 50, 150),
    BEAR_POLAR("bear polar", 90, 50, 150),
    MOOSE_BULL("moose bull", 100, 50, 150),
    MOOSE_COW("moose cow", 80, 40, 120),
    TOAD_RHINO_MAGMA("toad rhino magma", 100, 50, 150),
    TOAD_RHINO_GREEN("toad rhino green", 100, 50, 150),
    TOAD_RHINO("toad rhino", 90, 50, 150),
    MOSSHORN_PLAIN("mosshorn plain", 100, 50, 150),
    MOSSHORN("mosshorn", 90, 50, 150),
    // --- Prédateurs moyens (Lv30-120) ---
    WOLF_BLACK("wolf black", 60, 30, 120),
    WOLF_WHITE("wolf white", 60, 30, 120),
    LEOPARD_SNOW("leopard snow", 80, 40, 130),
    LIZARD_SAND("lizard sand", 60, 30, 120),
    SCORPION("scorpion", 80, 40, 130),
    CROCODILE("crocodile", 110, 50, 150),
    SPIDER_CAVE("spider cave", 50, 30, 100),
    SPIDER_BASE("spider", 40, 20, 80),
    SNAKE_COBRA("snake cobra", 50, 30, 100),
    SNAKE_RATTLE("snake rattle", 40, 20, 80),
    SNAKE_MARSH("snake marsh", 30, 20, 70),
    HYENA("hyena", 50, 30, 100),
    // --- Petits prédateurs / nuisibles (Lv20-60) ---
    FOX("fox", 20, 20, 60),
    LARVA_SILK("larva silk", 20, 20, 30),
    RAT("rat", 10, 20, 50),
    MOLERAT("molerat", 30, 20, 70),
    SLUG_MAGMA("slug magma", 50, 30, 100),
    SNAIL_MAGMA("snail magma", 40, 20, 80),
    SNAIL_FROST("snail frost", 40, 20, 80),
    CACTEE("cactee", 30, 20, 70),
    VULTURE("vulture", 40, 1, 30),
    HAWK("hawk", 30, 1, 30),
    OWL_SNOW("owl snow", 25, 1, 30),
    OWL_BROWN("owl brown", 25, 1, 30),
    RAVEN("raven", 30, 1, 30),
    RABBIT("rabbit", 30, 1, 30),
    CROW("crow", 15, 1, 30),
    PARROT("parrot", 20, 1, 30),
    FLAMINGO("flamingo", 35, 1, 30),
    PENGUIN("penguin", 25, 1, 30),
    DUCK("duck", 15, 1, 30),
    CHICKEN_DESERT_CHICK("chicken desert chick", 5, 1, 30),
    CHICKEN_DESERT("chicken desert", 20, 1, 30),
    CHICKEN_CHICK("chicken chick", 5, 1, 30),
    CHICKEN_BASE("chicken", 15, 1, 30),
    BLUEBIRD("bluebird", 10, 1, 30),
    FINCH_GREEN("finch green", 10, 1, 30),
    SPARROW("sparrow", 10, 1, 30),
    PIGEON("pigeon", 10, 1, 30),
    WOODPECKER("woodpecker", 10, 1, 30),
    FROG_BLUE("frog blue", 5, 1, 50),
    FROG_GREEN("frog green", 5, 1, 50),
    FROG_ORANGE("frog orange", 5, 1, 50),
    GECKO("gecko", 10, 1, 50),
    ARMADILLO("armadillo", 60, 1, 50),
    MEERKAT("meerkat", 20, 1, 50),
    SQUIRREL("squirrel", 10, 1, 50),
    MOUSE("mouse", 5, 1, 50),
    BAT_ICE("bat ice", 20, 1, 50),
    BAT("bat", 10, 1, 50),
    HORSE_SKELETON_ARMORED("horse skeleton armored", 120, 100, 200),
    HORSE_SKELETON("horse skeleton", 90, 50, 150),
    PIG_UNDEAD("pig undead", 40, 1, 20),
    COW_UNDEAD("cow undead", 50, 20, 200),
    CHICKEN_UNDEAD("chicken undead", 20, 1, 20),
    BUNNY("bunny", 20, 1, 20),
    TETRABIRD("tetrabird", 20, 1, 20),
    TORTOISE("tortoise", 20, 1, 20),
    HATWORM("hatworm", 15, 1, 50);

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
package com.aksharadeep.tutor.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.aksharadeep.tutor.models.Chapter;
import com.aksharadeep.tutor.models.Question;
import com.aksharadeep.tutor.models.QuizResult;
import com.aksharadeep.tutor.models.Subject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Subject.class, Chapter.class, Question.class, QuizResult.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SubjectDao subjectDao();
    public abstract ChapterDao chapterDao();
    public abstract QuestionDao questionDao();
    public abstract QuizResultDao quizResultDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "aksharadeep_db")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                AppDatabase database = INSTANCE;
                seedDatabase(database);
            });
        }
    };

    private static void seedDatabase(AppDatabase db) {
        SubjectDao subjectDao = db.subjectDao();
        ChapterDao chapterDao = db.chapterDao();
        QuestionDao questionDao = db.questionDao();

        // === INSERT SUBJECTS ===
        Subject science = new Subject("Science", "#00695C");
        Subject math = new Subject("Mathematics", "#6A1B9A");
        Subject social = new Subject("Social Studies", "#E65100");

        subjectDao.insertAll(science, math, social);

        // Get subjects to retrieve their IDs
        java.util.List<Subject> subjects = subjectDao.getAllSubjectsSync();
        int sciId = subjects.get(0).id;
        int mathId = subjects.get(1).id;
        int socId = subjects.get(2).id;

        // === INSERT SCIENCE CHAPTERS ===
        Chapter[] sciChapters = {
                new Chapter(sciId, "Chemical Reactions and Equations"),
                new Chapter(sciId, "Acids, Bases and Salts"),
                new Chapter(sciId, "Metals and Non-metals"),
                new Chapter(sciId, "Carbon and its Compounds"),
                new Chapter(sciId, "Life Processes"),
                new Chapter(sciId, "Control and Coordination"),
                new Chapter(sciId, "Electricity"),
                new Chapter(sciId, "Magnetic Effects of Electric Current"),
                new Chapter(sciId, "Light – Reflection and Refraction"),
                new Chapter(sciId, "The Human Eye and the Colourful World")
        };
        chapterDao.insertAll(sciChapters);

        // === INSERT MATH CHAPTERS ===
        Chapter[] mathChapters = {
                new Chapter(mathId, "Real Numbers"),
                new Chapter(mathId, "Polynomials"),
                new Chapter(mathId, "Pair of Linear Equations"),
                new Chapter(mathId, "Quadratic Equations"),
                new Chapter(mathId, "Arithmetic Progressions"),
                new Chapter(mathId, "Triangles"),
                new Chapter(mathId, "Coordinate Geometry"),
                new Chapter(mathId, "Introduction to Trigonometry"),
                new Chapter(mathId, "Some Applications of Trigonometry"),
                new Chapter(mathId, "Circles"),
                new Chapter(mathId, "Areas Related to Circles"),
                new Chapter(mathId, "Surface Areas and Volumes"),
                new Chapter(mathId, "Statistics"),
                new Chapter(mathId, "Probability")
        };
        chapterDao.insertAll(mathChapters);

        // === INSERT SOCIAL STUDIES CHAPTERS ===
        Chapter[] socChapters = {
                new Chapter(socId, "The Rise of Nationalism in Europe"),
                new Chapter(socId, "Nationalism in India"),
                new Chapter(socId, "The Making of a Global World"),
                new Chapter(socId, "The Age of Industrialisation"),
                new Chapter(socId, "Resources and Development"),
                new Chapter(socId, "Forest and Wildlife Resources"),
                new Chapter(socId, "Water Resources"),
                new Chapter(socId, "Agriculture"),
                new Chapter(socId, "Development"),
                new Chapter(socId, "Sectors of the Indian Economy"),
                new Chapter(socId, "Power Sharing"),
                new Chapter(socId, "Federalism"),
                new Chapter(socId, "Political Parties"),
                new Chapter(socId, "Outcomes of Democracy")
        };
        chapterDao.insertAll(socChapters);

        // === INSERT QUESTIONS ===
        java.util.List<Chapter> allChapters = chapterDao.getAllChaptersSync();

        // Science Questions (Chapter 1 - Chemical Reactions)
        int ch1 = allChapters.get(0).id;
        questionDao.insertAll(
                new Question(ch1, "What is the general formula for a chemical reaction?",
                        "A + B → C + D", "A × B = C", "A - B = C", "A / B = C", "A",
                        "A chemical reaction shows reactants on left and products on right, separated by an arrow."),
                new Question(ch1, "Which of the following is a sign of a chemical reaction?",
                        "Change in color", "No change", "Physical mixing", "Same smell", "A",
                        "Color change, gas evolution, precipitate formation, and temperature change indicate chemical reactions."),
                new Question(ch1, "Magnesium burns in oxygen to form:",
                        "MgO", "MgO2", "Mg2O", "MgCO3", "A",
                        "2Mg + O2 → 2MgO. Magnesium oxide is a white powder formed when Mg burns."),
                new Question(ch1, "A decomposition reaction is the opposite of:",
                        "Combination reaction", "Displacement", "Redox", "Precipitation", "A",
                        "Decomposition breaks one compound into two or more, while combination joins two into one."),
                new Question(ch1, "When iron rusts, it is an example of:",
                        "Oxidation", "Reduction", "Neutralization", "Decomposition", "A",
                        "Rusting of iron is oxidation — iron reacts with oxygen and water to form iron oxide (rust)."),
                new Question(ch1, "Photosynthesis is an example of:",
                        "Endothermic reaction", "Exothermic reaction", "Displacement", "Neutralization", "A",
                        "Photosynthesis absorbs light energy (endothermic) to produce glucose from CO2 and water."),
                new Question(ch1, "In a balanced equation, the number of atoms is equal on:",
                        "Both sides", "Only left side", "Only right side", "Neither side", "A",
                        "Law of conservation of mass: atoms are neither created nor destroyed in a reaction.")
        );

        // Science Questions (Chapter 2 - Acids, Bases and Salts)
        int ch2 = allChapters.get(1).id;
        questionDao.insertAll(
                new Question(ch2, "The pH of a neutral solution is:",
                        "7", "0", "14", "5", "A",
                        "pH 7 is neutral. Below 7 is acidic, above 7 is basic."),
                new Question(ch2, "Which ion is responsible for acidic properties?",
                        "H+ (Hydrogen ion)", "OH- ion", "Na+ ion", "Cl- ion", "A",
                        "Acids release H+ ions in solution, which are responsible for their acidic nature."),
                new Question(ch2, "Sodium hydroxide (NaOH) is a:",
                        "Strong base", "Weak acid", "Neutral salt", "Strong acid", "A",
                        "NaOH is a strong base that completely dissociates in water."),
                new Question(ch2, "What does a litmus test show?",
                        "Whether a substance is acidic or basic", "Its color", "Its taste", "Its smell", "A",
                        "Red litmus turns blue in base, blue litmus turns red in acid."),
                new Question(ch2, "Baking soda is chemically known as:",
                        "Sodium bicarbonate", "Sodium chloride", "Calcium carbonate", "Potassium nitrate", "A",
                        "Baking soda is NaHCO3 — sodium hydrogen carbonate (sodium bicarbonate).")
        );

        // Math Questions (Chapter 1 - Real Numbers)
        int mch1 = allChapters.get(10).id;
        questionDao.insertAll(
                new Question(mch1, "What is the HCF of 12 and 18?",
                        "6", "12", "18", "3", "A",
                        "Factors of 12: 1,2,3,4,6,12. Factors of 18: 1,2,3,6,9,18. HCF = 6."),
                new Question(mch1, "Which of the following is an irrational number?",
                        "√2", "3/4", "0.5", "9/3", "A",
                        "√2 cannot be expressed as p/q, so it is irrational. 3/4, 0.5, 9/3=3 are rational."),
                new Question(mch1, "The product of HCF and LCM of two numbers equals:",
                        "Product of the two numbers", "Sum of the numbers", "Difference", "HCF squared", "A",
                        "HCF × LCM = Product of two numbers. This is a fundamental theorem."),
                new Question(mch1, "Every composite number can be expressed as a product of:",
                        "Primes (Fundamental Theorem)", "Even numbers", "Odd numbers", "Multiples of 2", "A",
                        "Fundamental Theorem of Arithmetic: every composite number is uniquely a product of primes."),
                new Question(mch1, "π (pi) is a/an:",
                        "Irrational number", "Rational number", "Integer", "Natural number", "A",
                        "π = 3.14159... is non-terminating non-repeating, hence irrational.")
        );

        // Math Questions (Chapter 4 - Quadratic Equations)
        int mch4 = allChapters.get(13).id;
        questionDao.insertAll(
                new Question(mch4, "The standard form of a quadratic equation is:",
                        "ax² + bx + c = 0", "ax + b = 0", "ax³ + b = 0", "a/x + b = 0", "A",
                        "Quadratic equations have degree 2, written as ax² + bx + c = 0 where a ≠ 0."),
                new Question(mch4, "The discriminant of ax² + bx + c = 0 is:",
                        "b² - 4ac", "b² + 4ac", "b + 4ac", "b - 4ac", "A",
                        "Discriminant D = b² - 4ac determines the nature of roots."),
                new Question(mch4, "If discriminant = 0, the roots are:",
                        "Equal and real", "Unequal real", "Imaginary", "Zero", "A",
                        "When D = 0, the equation has two equal real roots."),
                new Question(mch4, "The roots of x² - 5x + 6 = 0 are:",
                        "2 and 3", "1 and 6", "2 and 6", "3 and 4", "A",
                        "x² - 5x + 6 = (x-2)(x-3) = 0. So x = 2 or x = 3."),
                new Question(mch4, "Which formula gives the roots of a quadratic?",
                        "Quadratic formula: (-b ± √D) / 2a", "b/a", "c/a", "-b/2a only", "A",
                        "The quadratic formula x = (-b ± √(b²-4ac)) / 2a gives both roots.")
        );

        // Social Studies Questions (Chapter 1 - Rise of Nationalism)
        int sch1 = allChapters.get(24).id;
        questionDao.insertAll(
                new Question(sch1, "Who painted 'Allegory of Italy' representing nationalism?",
                        "Frédéric Sorrieu", "Napoleon", "Garibaldi", "Mazzini", "A",
                        "Frédéric Sorrieu, a French artist, painted a series showing his vision of democratic nations."),
                new Question(sch1, "The French Revolution began in:",
                        "1789", "1800", "1815", "1750", "A",
                        "The French Revolution of 1789 marked a turning point — it introduced ideas of liberty and nationalism."),
                new Question(sch1, "The Treaty of Vienna (1815) was signed after:",
                        "Napoleon's defeat", "French Revolution", "World War I", "Unification of Germany", "A",
                        "After Napoleon's defeat, European powers met in Vienna in 1815 to restore the old order."),
                new Question(sch1, "Giuseppe Mazzini founded 'Young Italy' to:",
                        "Unite Italy as a republic", "Support monarchy", "Fight France", "Help Britain", "A",
                        "Giuseppe Mazzini was a revolutionary who wanted to unite Italy as a republic."),
                new Question(sch1, "The German Empire was proclaimed in:",
                        "1871", "1848", "1815", "1900", "A",
                        "After Prussia's wars, the German Empire was proclaimed in the Hall of Mirrors, Versailles in 1871.")
        );

        // Social Studies - Nationalism in India
        int sch2 = allChapters.get(25).id;
        questionDao.insertAll(
                new Question(sch2, "The Non-Cooperation Movement was launched in:",
                        "1920", "1919", "1930", "1942", "A",
                        "Gandhi launched the Non-Cooperation Movement in 1920 against British rule."),
                new Question(sch2, "The Rowlatt Act (1919) allowed:",
                        "Detention without trial", "Free elections", "Press freedom", "Trade rights", "A",
                        "The Rowlatt Act allowed detention without trial, sparking widespread protests."),
                new Question(sch2, "The Jallianwala Bagh massacre happened in:",
                        "1919", "1920", "1915", "1930", "A",
                        "On April 13, 1919, General Dyer ordered troops to fire at a peaceful gathering in Amritsar."),
                new Question(sch2, "Civil Disobedience Movement began with:",
                        "Dandi March (Salt March)", "Quit India", "Swadeshi", "Khilafat", "A",
                        "Gandhi began the Civil Disobedience Movement by walking 240 miles to Dandi to make salt in 1930."),
                new Question(sch2, "The Indian National Congress was founded in:",
                        "1885", "1905", "1920", "1857", "A",
                        "The INC was founded in 1885 by A.O. Hume, initially as a moderate reform body.")
        );

        // Science - Life Processes
        int sciCh5 = allChapters.get(4).id;
        questionDao.insertAll(
                new Question(sciCh5, "Photosynthesis occurs in which organelle?",
                        "Chloroplast", "Mitochondria", "Nucleus", "Ribosome", "A",
                        "Chloroplasts contain chlorophyll which captures sunlight for photosynthesis."),
                new Question(sciCh5, "Which gas is released during photosynthesis?",
                        "Oxygen", "Carbon dioxide", "Nitrogen", "Hydrogen", "A",
                        "6CO2 + 6H2O → C6H12O6 + 6O2. Oxygen is released as a byproduct."),
                new Question(sciCh5, "The breakdown of glucose in the absence of oxygen is called:",
                        "Anaerobic respiration", "Aerobic respiration", "Photosynthesis", "Digestion", "A",
                        "Anaerobic respiration occurs without oxygen and produces lactic acid or ethanol."),
                new Question(sciCh5, "Kidneys are the main organs of:",
                        "Excretion", "Digestion", "Reproduction", "Circulation", "A",
                        "Kidneys filter blood and produce urine, removing urea and other waste products."),
                new Question(sciCh5, "The liquid part of blood is called:",
                        "Plasma", "Serum", "Hemoglobin", "Lymph", "A",
                        "Plasma is the yellow liquid component of blood carrying blood cells, nutrients and waste.")
        );

        // Math - Triangles
        int mch6 = allChapters.get(15).id;
        questionDao.insertAll(
                new Question(mch6, "Two triangles are similar if their corresponding angles are:",
                        "Equal", "Supplementary", "Complementary", "Different", "A",
                        "Similar triangles (AA criterion) have equal corresponding angles and proportional sides."),
                new Question(mch6, "The Pythagoras theorem states: In a right triangle,",
                        "Hypotenuse² = Base² + Height²", "Base = Height", "Hyp = Base + Height", "Area = Base × Height", "A",
                        "a² + b² = c² where c is the hypotenuse. This holds only in right-angled triangles."),
                new Question(mch6, "Basic Proportionality Theorem (BPT) is also called:",
                        "Thales theorem", "Pythagoras theorem", "Euclid theorem", "Angle theorem", "A",
                        "BPT/Thales theorem: if a line is drawn parallel to one side of a triangle, it divides the other two sides proportionally."),
                new Question(mch6, "The ratio of areas of two similar triangles equals:",
                        "Square of ratio of corresponding sides", "Ratio of sides", "Cube of sides", "Half the ratio", "A",
                        "Area ratio = (side ratio)². If sides are in ratio 2:3, areas are in ratio 4:9."),
                new Question(mch6, "In △ABC, if DE || BC, then AD/DB =",
                        "AE/EC", "AB/BC", "AC/BC", "DE/BC", "A",
                        "By Basic Proportionality Theorem: AD/DB = AE/EC when DE is parallel to BC.")
        );

        // More Science - Electricity
        int sciCh7 = allChapters.get(6).id;
        questionDao.insertAll(
                new Question(sciCh7, "Ohm's Law states: V =",
                        "I × R", "I / R", "I + R", "I - R", "A",
                        "Ohm's Law: Voltage = Current × Resistance (V = IR). Georg Ohm formulated this."),
                new Question(sciCh7, "The SI unit of electric resistance is:",
                        "Ohm (Ω)", "Ampere (A)", "Volt (V)", "Watt (W)", "A",
                        "Resistance is measured in Ohms (Ω), named after physicist Georg Ohm."),
                new Question(sciCh7, "In a series circuit, the total resistance is:",
                        "Sum of all resistances", "Less than smallest resistance", "Product of resistances", "Unchanged", "A",
                        "In series: R_total = R1 + R2 + R3. Adding resistors in series increases total resistance."),
                new Question(sciCh7, "Electric power is calculated as:",
                        "P = V × I", "P = V + I", "P = V / I", "P = I / V", "A",
                        "Power P = V × I = I²R = V²/R. SI unit is Watt (W)."),
                new Question(sciCh7, "1 kilowatt-hour (kWh) is the unit of:",
                        "Electrical energy", "Electrical power", "Resistance", "Current", "A",
                        "kWh is the commercial unit of electrical energy. 1 kWh = 3.6 × 10⁶ Joules.")
        );
    }
}

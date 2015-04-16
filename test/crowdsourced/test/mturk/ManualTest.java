package crowdsourced.test.mturk;

import java.util.ArrayList;
import java.util.List;

import crowdsourced.mturk.AMTCommunicator;
import crowdsourced.mturk.BooleanChoiceQuestion;
import crowdsourced.mturk.HIT;
import crowdsourced.mturk.MultipleChoiceOption;
import crowdsourced.mturk.MultipleChoiceQuestion;
import crowdsourced.mturk.NumericQuestion;
import crowdsourced.mturk.PendingJob;
import crowdsourced.mturk.Question;
import crowdsourced.mturk.StringQuestion;
import crowdsourced.mturk.URLQuestion;

/**
 * An extensive manual test.
 * I have decided not to use JUnit for now, as the answers need to be inserted
 * into the AMT Sandbox manually.
 *
 * @author Florian Chlan
 *
 */
public class ManualTest {

    public static void startTestHITTask() {
        List<Question> quests = new ArrayList<Question>();

        Question string = new StringQuestion(
                "string", "Restaurants in Lausanne",
                "Please enter the name of a restaurant in Lausanne. It could be any restaurant.");
        quests.add(string);

        StringQuestion stringAnswerLength = new StringQuestion(
                "stringAnswerLength", "Restaurants in Lausanne",
                "Blablabla min 10 chars, max 25 chars");
        stringAnswerLength.setAnswerLength(10, 25);
        quests.add(stringAnswerLength);

        StringQuestion stringSuggestedLines = new StringQuestion(
                "stringSuggestedLines", "Restaurants in Lausanne",
                "Blablabla 8 lines suggested, min 50 chars, max 75 chars");
        stringSuggestedLines.setAnswerLength(50, 75);
        stringSuggestedLines.setSuggestedLineCount(8);
        quests.add(stringSuggestedLines);
        StringQuestion stringDefaultText = new StringQuestion(
                "stringDefaultText", "Restaurants in Lausanne", "Blabla default text");
        stringDefaultText.setDefaultText("Write here. Don't write bullshit!");
        quests.add(stringDefaultText);

        Question url = new URLQuestion("url", "Sailing in Cote d'Ivoire", "Find a website and write the URL.");
        quests.add(url);

        Question numericDefault = new NumericQuestion(
                "numericDefault", "Count Swiss cantons", "How many cantons are there in Switzerland?");
        quests.add(numericDefault);

        Question numericRange= new NumericQuestion(
                8, 10, "numericRange", "Count Austrian federal states",
                "How many federal states are there in Austria? (min 8 max 10)");
        quests.add(numericRange);

        Question booleanQuestion = new BooleanChoiceQuestion(
                "booleanQuestion", "Christmas",
                "Answer if the following sentence is correct: Today is christmas.");
        quests.add(booleanQuestion);

        List<MultipleChoiceOption> options = new ArrayList<MultipleChoiceOption>();
        options.add(new MultipleChoiceOption("triangle", "I am a triangle."));
        options.add(new MultipleChoiceOption("cube", "I am a cube."));
        options.add(new MultipleChoiceOption("sphere", "I am a sphere."));
        Question multipleQuestion = new MultipleChoiceQuestion(
                "multipleQuestion", "Chose a geometric shape", "Blablabla", options);
        quests.add(multipleQuestion);

        List<String> kws = new ArrayList<String>();
        kws.add("restaurant");
        kws.add("lausanne");
        HIT hit = new HIT(
                "Answer various questions", "Answer various questions of different type.",
                quests, 900, 5, 0.01f, 600, kws);

        PendingJob job = AMTCommunicator.sendHIT(hit, new ManualTestCallback());
        try {
            Thread.sleep(600 * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}

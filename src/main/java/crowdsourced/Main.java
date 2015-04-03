package crowdsourced;

/**
 * Main class lauched when running the application.
 *
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello");
        System.exit(0);
        /*
         * The following example shows the general idea of getting the answers from the workers.
         * Please keep in mind, that certain methods can still change
         * (but of course I'll do my best that they won't).
         *
         * Let's do some definitions:
         * An answer is a result for one particular question.
         * There can be several questions in a HIT.
         * We normally make all our questions mandatory.
         * An assignment is a set of answers to
         * each and everyone of the questions in a particular HIT.
         * So in an assignment, there will be as many answers as there are questions in the HIT.
         *
         * The first thing you need to do is implement the AnswerCallback interface.
         * You will pass an implementation of AnswerCallback with the HIT to AMTCommunicator.
         * (see AMTCommunicator.sendHIT(HIT, AnswerCallback) method.
         *
         * The interface has 3 methods. Everytime we receive new answers/assignments,
         * we will call the newAssignmentsReceived()-method with a List of new Assignments.
         * You will see one particular assignment only once, so you should save the assignments
         * in your own data structure.
         *
         * So let's look at a single Assignment. This is just a wrapper for a Map<String, Answer>.
         * The key is the unique identifier of the question.
         * In the constructor of the Questions, there is an identifier-field, which should be unique
         * in this HIT.
         *
         * So if you take the identifier of the question,
         * you can look up the answer that the worker gave. Just use the map for lookup.
         *
         * You then have the answer object. Take a look at the
         * Answer.getAnswer(AnswerVisitor) method.
         * AnswerVisitor is an interface, which you have to implement.
         * If you invoke the getAnswer-method, you will pass one of your
         * implementations of AnswerVisitor. Then, the corresponding
         * visit-Method in your implementation will be called.
         * So for example, if the question is a StringQuestion,
         * the Answer will be returned by the visit(String answer)-method.
         * For each type of answer type, there is a different visit-method.
         * Please look up the visitor pattern for more information.
         *
         *
         */
    }
}

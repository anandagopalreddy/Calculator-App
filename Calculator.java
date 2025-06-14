import java.util.*;

public class Calculator {

    static class InvalidExpressionException extends Exception {
        private final int errorPosition;

        public InvalidExpressionException(String message) {
            this(message, -1);
        }

        public InvalidExpressionException(String message, int errorPosition) {
            super(message);
            this.errorPosition = errorPosition;
        }

        public int getErrorPosition() {
            return errorPosition;
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            run(scanner);
        }
        System.out.println("\nThank you for using the Unified Calculator. Goodbye!");
    }

    public static void run(Scanner scanner) {
        System.out.println("\n--- Starting Unified Console Calculator ---");
        System.out.println("Welcome to the Unified Calculator!");

        while (true) {
            List<Object> tokens = null;
            double result = 0.0;
            String expr = "";

            while (tokens == null) {
                try {
                    System.out.print("\nEnter a mathematical expression (or type 'exit' to quit): ");
                    expr = scanner.nextLine();
                    if (expr.equalsIgnoreCase("exit") || expr.equalsIgnoreCase("quit")) {
                        System.out.println("\n--- Console Calculator Exited ---");
                        return;
                    }
                    List<Object> tempTokens = tokenizeExpression(expr);
                    result = evaluate(new ArrayList<>(tempTokens));
                    tokens = tempTokens;
                    System.out.println("Expression is valid and computable.");
                } catch (InvalidExpressionException e) {
                    System.out.println("Syntax Error: " + e.getMessage());
                    if (e.getErrorPosition() != -1) {
                        System.out.println("Input: " + expr);
                        System.out.print("       ");
                        for (int i = 0; i < e.getErrorPosition(); i++) System.out.print(" ");
                        System.out.println("^");
                    }
                    System.out.println("Please try again.");
                } catch (ArithmeticException e) {
                    System.out.println("Calculation Error: " + e.getMessage());
                    System.out.println("Input: " + expr);
                    System.out.println("This expression cannot be computed. Please try again.");
                }
            }

            List<Double> allNumbers = new ArrayList<>();
            List<Double> evenNumbers = new ArrayList<>();
            List<Double> oddNumbers = new ArrayList<>();
            for (Object token : tokens) {
                if (token instanceof Double) {
                    double num = (Double) token;
                    allNumbers.add(num);
                    if (num % 1 == 0) {
                        if ((long) num % 2 == 0) {
                            evenNumbers.add(num);
                        } else {
                            oddNumbers.add(num);
                        }
                    }
                }
            }

            boolean representationChosen = false;
            while (!representationChosen) {
                System.out.println("\nWhat would you like to do with this expression?");
                System.out.println("1. Represent as a LinkedList (with link format)");
                System.out.println("2. Represent as a Queue (with 'Queue Sink')");
                System.out.println("3. Represent as a simple ArrayList");
                System.out.println("4. Enter a new expression");
                System.out.println("5. Quit (Exit Program)");
                System.out.print("Enter your choice (1-5): ");

                try {
                    String choiceStr = scanner.nextLine();
                    int mode = Integer.parseInt(choiceStr);
                    switch (mode) {
                        case 1:
                            handleLinkedListMode(tokens, evenNumbers, oddNumbers, result);
                            break;
                        case 2:
                            System.out.print("Enter capacity for input number queues: ");
                            int inCap = Integer.parseInt(scanner.nextLine());
                            System.out.print("Enter capacity for even/odd queues: ");
                            int eoCap = Integer.parseInt(scanner.nextLine());
                            handleQueueMode(tokens, allNumbers, evenNumbers, oddNumbers, inCap, eoCap, result);
                            break;
                        case 3:
                            handleArrayListMode(tokens, evenNumbers, oddNumbers, result);
                            break;
                        case 4:
                            representationChosen = true;
                            break;
                        case 5:
                            System.out.println("\n--- Console Calculator Exited ---");
                            return;
                        default:
                            System.out.println("Invalid choice. Please select 1-5.");
                    }
                    if (mode >= 1 && mode <= 3) {
                        System.out.println("----------------------------------------");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number for your choice or capacity.");
                }
            }
        }
    }

    private static void handleArrayListMode(List<Object> tokens, List<Double> even, List<Double> odd, double result) {
        ArrayList<Object> expressionList = new ArrayList<>(tokens);
        System.out.println("\nRepresentation: " + expressionList);
        System.out.println("Result: " + result);
        System.out.println("Even Numbers: " + even);
        System.out.println("Odd Numbers: " + odd);
    }

    private static void handleLinkedListMode(List<Object> tokens, List<Double> even, List<Double> odd, double result) {
        LinkedList<Object> expressionList = new LinkedList<>(tokens);
        System.out.println();
        printAsLinks(expressionList, "Representation");
        System.out.println("Result: " + result);
        printAsLinks(new ArrayList<>(even), "Even Numbers");
        printAsLinks(new ArrayList<>(odd), "Odd Numbers");
    }

    private static void handleQueueMode(List<Object> tokens, List<Double> allNumbers, List<Double> evenNumbers, List<Double> oddNumbers, int inCap, int eoCap, double result) {
        Queue<Object> expressionQueue = new LinkedList<>(tokens);
        LinkedList<Queue<Double>> inputQueues = new LinkedList<>();
        LinkedList<Queue<Double>> evenQueues = new LinkedList<>();
        LinkedList<Queue<Double>> oddQueues = new LinkedList<>();
        System.out.println("\nSinking all numbers from the expression into queues of capacity " + inCap + "...");
        for (Double number : allNumbers) addToQueueList(inputQueues, number, inCap);
        System.out.println("Sinking even/odd numbers into separate queues of capacity " + eoCap + "...");
        evenNumbers.forEach(n -> addToQueueList(evenQueues, n, eoCap));
        oddNumbers.forEach(n -> addToQueueList(oddQueues, n, eoCap));
        System.out.println("\nFull Expression Queue: " + expressionQueue);
        System.out.println("Result: " + result);
        System.out.println("\n--- Queue Sink Results ---");
        System.out.println("Input Number Queues (Capacity: " + inCap + "):");
        printQueueList(inputQueues);
        System.out.println("Even Number Queues (Capacity: " + eoCap + "):");
        printQueueList(evenQueues);
        System.out.println("Odd Number Queues (Capacity: " + eoCap + "):");
        printQueueList(oddQueues);
    }

    private static List<Object> tokenizeExpression(String expr) throws InvalidExpressionException {
        List<Object> tokens = new ArrayList<>();
        int parenthesisBalance = 0;
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) continue;
            if (Character.isDigit(c) || c == '.') {
                int startPos = i;
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) sb.append(expr.charAt(i++));
                i--;
                try {
                    tokens.add(Double.parseDouble(sb.toString()));
                } catch (NumberFormatException e) {
                    throw new InvalidExpressionException("Invalid number format: '" + sb + "'", startPos);
                }
            } else if ("+-*/()".indexOf(c) != -1) {
                tokens.add(c);
                if (c == '(') parenthesisBalance++;
                else if (c == ')') parenthesisBalance--;
                if (parenthesisBalance < 0) throw new InvalidExpressionException("Mismatched parentheses: Extra ')' found.", i);
            } else {
                throw new InvalidExpressionException("Invalid character in expression: '" + c + "'", i);
            }
        }
        if (parenthesisBalance != 0) throw new InvalidExpressionException("Mismatched parentheses: Not all '(' were closed.", expr.length());
        return tokens;
    }

    private static double evaluate(Iterable<Object> tokens) throws InvalidExpressionException {
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();
        try {
            for (Object token : tokens) {
                if (token instanceof Double) values.push((Double) token);
                else if (token instanceof Character) {
                    char op = (Character) token;
                    if (op == '(') ops.push(op);
                    else if (op == ')') {
                        while (ops.peek() != '(') values.push(applyOperation(ops.pop(), values.pop(), values.pop()));
                        ops.pop();
                    } else {
                        while (!ops.empty() && hasPrecedence(op, ops.peek())) values.push(applyOperation(ops.pop(), values.pop(), values.pop()));
                        ops.push(op);
                    }
                }
            }
            while (!ops.empty()) values.push(applyOperation(ops.pop(), values.pop(), values.pop()));
            if (values.size() != 1 || !ops.isEmpty()) throw new InvalidExpressionException("Malformed expression: Check operators and operands.");
            return values.pop();
        } catch (EmptyStackException e) {
            throw new InvalidExpressionException("Malformed expression: Operator is missing an operand.");
        }
    }

    private static int getPrecedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }

    private static boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') return false;
        return getPrecedence(op1) <= getPrecedence(op2);
    }

    private static double applyOperation(char op, double b, double a) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': if (b == 0) throw new ArithmeticException("Division by zero."); return a / b;
        }
        return 0;
    }

    private static void addToQueueList(LinkedList<Queue<Double>> list, double num, int capacity) {
        if (capacity <= 0) return;
        if (list.isEmpty() || list.getLast().size() >= capacity) list.add(new LinkedList<>());
        list.getLast().add(num);
    }

    private static void printQueueList(LinkedList<Queue<Double>> queues) {
        if (queues.isEmpty()) {
            System.out.println("  [None]");
            return;
        }
        int i = 1;
        for (Queue<Double> q : queues) System.out.println("  Queue " + (i++) + ": " + q);
    }

    private static void printAsLinks(Collection<?> collection, String label) {
        System.out.print(label + ": ");
        if (collection.isEmpty()) {
            System.out.println("null");
            return;
        }
        StringJoiner sj = new StringJoiner(" -> ");
        collection.forEach(item -> sj.add(item.toString()));
        System.out.println(sj.toString() + " -> null");
    }
}

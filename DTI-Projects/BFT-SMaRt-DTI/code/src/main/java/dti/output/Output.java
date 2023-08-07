package dti.output;

import dti.models.NFT;
import dti.models.NFTRequest;
import dti.utils.Pair;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Output {

    private static final List<String> COINS_INFO_COLUMNS_NAME = Arrays.asList("ID", "Value");
    private static final List<String> NFT_INFO_COLUMNS_NAME = Arrays.asList("ID", "Name", "URI", "No of requests");
    private static final List<String> NFT_REQUESTS_COLUMNS_NAME = Arrays.asList("Issuer", "Name", "Offered value", "Validity");

    private static ArrayList<ArrayList<String>> valuesToPrint= new ArrayList<>();
    private static int topLength = 0;

    //longs, Pair<Long, Float>, nft, nftRequest
    public static <T> void printValues(ArrayList<T> values, OPERATIONS operation) {

        T fistValue = values.get(0);
        //The first value of each arraylist is the column name
        //The second value is the size needed to that column

        if (fistValue instanceof Pair) {
            pairsColumns(values);
        } else if (fistValue instanceof NFTRequest) {
            nftRequestsColumns(values, operation);
        } else if (fistValue instanceof NFT) {
            nftColumns(values, operation);
        } else return;

        printStructure();
        cleanLists();
    }

    private static void cleanLists() {
        valuesToPrint = new ArrayList<>();
    }

    private static <T> void pairsColumns(ArrayList<T> values) {
        ArrayList<String> firstColumn = new ArrayList<>();
        ArrayList<String> secondColumn = new ArrayList<>();

        String firstColumnName = Output.COINS_INFO_COLUMNS_NAME.get(0);
        String secondColumnName = Output.COINS_INFO_COLUMNS_NAME.get(1);

        int firstBiggestRow = firstColumnName.length();
        int secondBiggestRow = secondColumnName.length();

        firstColumn.add(firstColumnName);
        firstColumn.add(Integer.toString(firstBiggestRow));
        secondColumn.add(secondColumnName);
        secondColumn.add(Integer.toString(secondBiggestRow));

        for (T v: values) {
            Pair<Long, Float> aux = (Pair<Long, Float>)v;

            String firstValue = aux.getA().toString();
            String secondValue = aux.getB().toString();

            int valueLength = firstValue.length();
            if(valueLength > firstBiggestRow) {
                firstBiggestRow = valueLength;
                firstColumn.set(1, Integer.toString(firstBiggestRow));
            }
            firstColumn.add(firstValue);

            valueLength = secondValue.length();
            if(valueLength > secondBiggestRow) {
                secondBiggestRow = valueLength;
                secondColumn.set(1, Integer.toString(secondBiggestRow));
            }
            secondColumn.add(secondValue);
        }
        topLength = firstBiggestRow + secondBiggestRow;
        valuesToPrint.add(firstColumn);
        valuesToPrint.add(secondColumn);
    }
    private static <T> void nftColumns(ArrayList<T> values, OPERATIONS operation) {
        ArrayList<String> firstColumn = new ArrayList<>();
        ArrayList<String> secondColumn = new ArrayList<>();
        ArrayList<String> thirdColumn = new ArrayList<>();
        ArrayList<String> fourthColumn = new ArrayList<>();

        int firstBiggestRow = Output.NFT_INFO_COLUMNS_NAME.get(0).length();
        int secondBiggestRow = Output.NFT_INFO_COLUMNS_NAME.get(1).length();
        int thirdBiggestRow = Output.NFT_INFO_COLUMNS_NAME.get(2).length();
        int fourthBiggestRow = Output.NFT_INFO_COLUMNS_NAME.get(3).length();

        firstColumn.add(Output.NFT_INFO_COLUMNS_NAME.get(0));
        firstColumn.add(Integer.toString(firstBiggestRow));
        secondColumn.add(Output.NFT_INFO_COLUMNS_NAME.get(1));
        secondColumn.add(Integer.toString(secondBiggestRow));
        thirdColumn.add(Output.NFT_INFO_COLUMNS_NAME.get(2));
        thirdColumn.add(Integer.toString(thirdBiggestRow));
        fourthColumn.add(Output.NFT_INFO_COLUMNS_NAME.get(3));
        fourthColumn.add(Integer.toString(fourthBiggestRow));

        for (T v: values) {
            NFT aux = (NFT) v;
            String firstValue = String.valueOf(aux.getId());
            String secondValue = String.valueOf(aux.getName());
            String thirdValue = String.valueOf(aux.getURI());
            String fourthValue = String.valueOf(aux.getRequests());

            int valueLength = firstValue.length();
            if(valueLength > firstBiggestRow) {
                firstBiggestRow = valueLength;
                firstColumn.set(1, Integer.toString(firstBiggestRow));
            }
            firstColumn.add(firstValue);

            valueLength = secondValue.length();
            if(valueLength > secondBiggestRow) {
                secondBiggestRow = valueLength;
                secondColumn.set(1, Integer.toString(secondBiggestRow));
            }
            secondColumn.add(secondValue);

            valueLength = thirdValue.length();
            if(valueLength > thirdBiggestRow) {
                thirdBiggestRow = valueLength;
                thirdColumn.set(1, Integer.toString(thirdBiggestRow));
            }
            thirdColumn.add(thirdValue);

            if(!operation.equals(OPERATIONS.EXISTING_NFT)) {
                valueLength = fourthValue.length();
                if (valueLength > fourthBiggestRow) {
                    fourthBiggestRow = valueLength;
                    fourthColumn.set(1, Integer.toString(fourthBiggestRow));
                }
                fourthColumn.add(fourthValue);
            }
        }
        topLength = firstBiggestRow + secondBiggestRow + thirdBiggestRow;
        valuesToPrint.add(firstColumn);
        valuesToPrint.add(secondColumn);
        valuesToPrint.add(thirdColumn);

        if(!operation.equals(OPERATIONS.EXISTING_NFT)) {
            topLength += fourthBiggestRow;
            valuesToPrint.add(fourthColumn);
        }
    }
    private static <T> void nftRequestsColumns(ArrayList<T> values, OPERATIONS operation) {
        ArrayList<String> firstColumn = new ArrayList<>();
        ArrayList<String> secondColumn = new ArrayList<>();
        ArrayList<String> thirdColumn = new ArrayList<>();
        ArrayList<String> fourthColumn = new ArrayList<>();

        String firstColumnName = Output.NFT_REQUESTS_COLUMNS_NAME.get(0);
        String secondColumnName = Output.NFT_REQUESTS_COLUMNS_NAME.get(1);
        String thirdColumnName = Output.NFT_REQUESTS_COLUMNS_NAME.get(2);
        String fourthColumnName = Output.NFT_REQUESTS_COLUMNS_NAME.get(3);

        int firstBiggestRow = firstColumnName.length();
        int secondBiggestRow = secondColumnName.length();
        int thirdBiggestRow = thirdColumnName.length();
        int fourthBiggestRow = fourthColumnName.length();

        firstColumn.add(firstColumnName);
        firstColumn.add(Integer.toString(firstBiggestRow));
        secondColumn.add(secondColumnName);
        secondColumn.add(Integer.toString(secondBiggestRow));
        thirdColumn.add(thirdColumnName);
        thirdColumn.add(Integer.toString(thirdBiggestRow));
        fourthColumn.add(fourthColumnName);
        fourthColumn.add(Integer.toString(fourthBiggestRow));

        for (T v: values) {
            NFTRequest aux = (NFTRequest) v;
            String firstValue = String.valueOf(aux.getIssuer());
            String secondValue = String.valueOf(aux.getRequestedNFT());
            String thirdValue = String.valueOf(aux.getOfferedValue());
            String fourthValue = new Timestamp(aux.getValidity()).toString();

            int valueLength = firstValue.length();
            if(valueLength > firstBiggestRow) {
                firstBiggestRow = valueLength;
                firstColumn.set(1, Integer.toString(firstBiggestRow));
            }
            firstColumn.add(firstValue);

            if(operation.equals(OPERATIONS.MY_NFT_REQUESTS)) {
                valueLength = secondValue.length();
                if (valueLength > secondBiggestRow) {
                    secondBiggestRow = valueLength;
                    secondColumn.set(1, Integer.toString(secondBiggestRow));
                }
                secondColumn.add(secondValue);
            }

            valueLength = thirdValue.length();
            if(valueLength > thirdBiggestRow) {
                thirdBiggestRow = valueLength;
                thirdColumn.set(1, Integer.toString(thirdBiggestRow));
            }
            thirdColumn.add(thirdValue);

            valueLength = fourthValue.length();
            if(valueLength > fourthBiggestRow) {
                fourthBiggestRow = valueLength;
                fourthColumn.set(1, Integer.toString(fourthBiggestRow));
            }
            fourthColumn.add(fourthValue);
        }
        topLength = firstBiggestRow + thirdBiggestRow + fourthBiggestRow;

        if(operation.equals(OPERATIONS.MY_NFT_REQUESTS)) {
            topLength += secondBiggestRow;
            valuesToPrint.add(secondColumn);
        }

        valuesToPrint.add(firstColumn);
        valuesToPrint.add(thirdColumn);
        valuesToPrint.add(fourthColumn);
    }

    private static void printStructure() {
        printBreakLine(topLength, valuesToPrint.size());

        for (int i = 0; i < valuesToPrint.get(0).size(); i++) {
            if(i == 1) continue;
            for (ArrayList<String> strings : valuesToPrint) {
                System.out.print("|");

                int length = strings.get(i).length();
                int rest = Integer.parseInt(strings.get(1)) - length + 2;
                int firstSpace = rest / 2;
                int secondSpace = firstSpace;
                if(rest %2 != 0) secondSpace++;

                for (int j = 0; j < firstSpace; j++) System.out.print(" ");
                System.out.print(strings.get(i));
                for (int j = 0; j < secondSpace; j++) System.out.print(" ");
            }
            System.out.print("|\n");
            printBreakLine(topLength, valuesToPrint.size());
        }
    }

    private static void printBreakLine(int size, int columnsNumber) {
        for (int i = 0; i < size + 4 + (3 * (columnsNumber - 1)); i++) {
            if (i == 0 || i == size + 3 + (3 * (columnsNumber - 1))) {
                System.out.print("+");
            } else {
                System.out.print("-");
            }
        }
        System.out.println();
    }
}

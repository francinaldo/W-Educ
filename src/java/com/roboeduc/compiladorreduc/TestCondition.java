/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.roboeduc.compiladorreduc;

/**
 *
 * @author carlafernandes
 */
public class TestCondition {
    private analisadorSintatico sintatico;
    private boolean enableComparison = false;
    public TestCondition(analisadorSintatico sintatico) {
        this.sintatico = sintatico;
    }
    
    public void testVariableCondition(int type) {
        boolean repeatDefault = false;
        if (getName(getPosition()+1).equals(")")) {
            if (getName(getPosition()+2).equals("{")) {
                if (getMapeamento().isBooleanFunction(getName(getPosition())) ||
                        getMapeamento().isNameFunction(getName(getPosition())) ||
                        getMapeamento().isNumberFunction(getName(getPosition()))) {
                    checkParameters(getName(getPosition()), 3);
                }
                else if (getMapeamento().isNameDefine(getName(getPosition())) ||
                        getMapeamento().isNumberDefine(getName(getPosition())) ||
                        getMapeamento().isBooleanDefine(getName(getPosition()))) {
                    writeOnFile(getMapeamento().getDefineText(getName(getPosition())));
                }
                else {
                    writeOnFile(getName(getPosition()));
                    setPosition(3);
                }
                writeOnFile(switchCondition()[1]+"\n");
                while ((getPosition()-1) < getListSize() && !getError() && !getName(getPosition()).equals("}")) {
                    if (getName(getPosition()).equals("se")) {
                        writeOnFile(switchCondition()[2]);
                        switch (type) {
                            case (1):
                                if (isNumber(getName(getPosition()+1)) || getMapeamento().isNumberDefine(getName(getPosition()+1))) {
                                    writeOnFile(getName(getPosition()+1));
                                    setPosition(2);
                                }
                                else {
                                    setErrorType("condicao");
                                    errorFunction(getLine(getPosition()+1),"20 - É exigido um número nesta expressão.");
                                }
                                break;
                            case (2):
                                if (isName(getPosition()+1)>0) {
                                    writeOnFile(getName(getPosition()+1));
                                    setPosition(2);
                                    writeString(false);
                                }
                                else {
                                    setErrorType("sintaxe condicao");
                                    errorFunction(getLine(getPosition()+1),"11 - Está faltando \".");
                                }
                                break;
                            case (3):
                                if (isBoolean(getName(getPosition()+1))) {
                                    writeBoolean(getName(getPosition()+1));
                                    setPosition(2);
                                }
                                break;
                        }
                        if (!getError()) {
                            if (getName(getPosition()).equals(":")) {
                                if (getName(getPosition()+1).equals("{")) {
                                    writeOnFile(switchCondition()[3]+"\n");
                                    setPosition(2);
                                    while ((getPosition()-1) < getListSize() && !getName(getPosition()).equals("}") && !getError()) {
                                        wordTest();
                                    }
                                    if (!getError()) {
                                        setPosition(1);
                                        writeOnFile("\n"+switchCondition()[4]+"\n");
                                    }
                                }
                                else {
                                    setErrorType("sintaxe condicao");
                                    errorFunction(getLine(getPosition()+1),"4 - Falta '{'.");
                                }
                            }
                            else {
                                setErrorType("sintaxe condicao");
                                errorFunction(getLine(getPosition()),"25 - Está faltando ':'.");
                            }
                        }
                    }
                    else if (getName(getPosition()).equals("senao")) {
                        if (!repeatDefault) {
                            repeatDefault = true;
                            writeOnFile(switchCondition()[5]);
                            setPosition(1);
                            if (getName(getPosition()).equals(":")) {
                                if (getName(getPosition()+1).equals("{")) {
                                    setPosition(2);
                                    while ((getPosition()-1) < getListSize() && !getName(getPosition()).equals("}") && !getError()) {
                                        wordTest();
                                    }
                                    if (!getError()) {
                                        setPosition(1);
                                        writeOnFile("\n"+switchCondition()[6]+"\n");
                                    }
                                }
                                else {
                                    setErrorType("sintaxe condicao");
                                    errorFunction(getLine(getPosition()+1),"4 - Falta '{'.");
                                }
                            }
                            else {
                                setErrorType("sintaxe condicao");
                                errorFunction(getLine(getPosition()),"25 - Está faltando ':'.");
                            }
                        }
                        else {
                            setErrorType("sintaxe condicao");
                            errorFunction(getLine(getPosition()),"26 - SENAO em duplicidade.");
                        }
                    }
                    else {
                        setPosition(1);
                        if (getPosition() == getListSize()) {
                            setErrorType("sintaxe condicao");
                            errorFunction(getLine(getPosition()-1),"6 - FIM não encontrado.");
                            break;
                        }
                    }
                }
                if (!getError()) {
                    writeOnFile("\n"+switchCondition()[7]+"\n");
                    setPosition(1);
                }
            }
            else {
                setErrorType("sintaxe condicao");
                errorFunction(getLine(getPosition()+2),"4 - Falta '{'.");
            }
        }
        else {
            setErrorType("sintaxe condicao");
            errorFunction(getLine(getPosition()+1),"15 - Está faltando ')'.");
        }
    }

    public void writeNumberCondition() {
        if (sintatico.isValidNumberExpression(0)) {
            if (getMapeamento().isNumberFunction(getName(getPosition()))) {
                checkParameters(getName(getPosition()), 2);
                if (sintatico.writeNumericalOperator(0)) {
                    writeOnFile(getName(getPosition()));
                    setPosition(1);
                    writeNumberCondition();
                }
            }
            else if (getMapeamento().isNumberDefine(getName(getPosition()))) {
                writeOnFile(getMapeamento().getDefineText(getName(getPosition())));
                if (sintatico.writeNumericalOperator(1)) {
                    setPosition(1);
                    writeOnFile(getName(getPosition()));
                    setPosition(1);
                    writeNumberCondition();
                }
                else {
                    setPosition(1);
                }
            }
            else {
                writeOnFile(getName(getPosition()));
                if (sintatico.writeNumericalOperator(1)) {
                    setPosition(1);
                    writeOnFile(getName(getPosition()));
                    setPosition(1);
                    writeNumberCondition();
                }
                else {
                    setPosition(1);
                }
            }
        }
    }
    
    public void testCondition() {
        if (getName(getPosition()).equals(")")) {
            if (getBracketsCounter() > 0) {
                if (getBracketsCounter() != 1) {
                    writeOnFile(")");
                }
                setBracketsCounter(-1);
                setPosition(1);
            }
            else {
                setErrorType("sintaxe estrutura");
                errorFunction(getLine(getPosition()),"16 - Confira os parênteses.");
            }
        }
        else if (getBracketsCounter() > 0) {
//            if (senses(getName(getPosition()))) {
//                analyzeSensor();
//                enableComparison = true;
//            }
            /*if (isNumberList(getName(getPosition())) || isNumber(getName(getPosition())) ||
                    getMapeamento().isNumberFunction(getName(getPosition())) ||
                    getMapeamento().isNumberDefine(getName(getPosition()))) {*/
            

            // Daqui pra baixo é antigo!!!

            // else if (sintatico.isValidNumberExpression(0)) {
            //     writeNumberCondition();
            //     //setPosition(1);
            //     writeNumberOperand();
            //     setPosition(1);
            //     writeNumberCondition();
            //     enableComparison = true;
            // }
            if (isNameList(getName(getPosition())) || isName(getPosition())>0 ||
                    getMapeamento().isNameFunction(getName(getPosition())) || getMapeamento().isNameDefine(getName(getPosition()))) {
                if (getMapeamento().isNameFunction(getName(getPosition()))) {
                    checkParameters(getName(getPosition()), 2);
                }
                else if (getMapeamento().isNameDefine(getName(getPosition()))) {
                    writeOnFile(getMapeamento().getDefineText(getName(getPosition())));
                    setPosition(1);
                }
                else if (isName(getPosition())>0) {
                    writeOnFile(getName(getPosition()));
                    setPosition(1);
                    writeString(false);
                }
                else {
                    writeOnFile(getName(getPosition()));
                    setPosition(1);
                }
                if (getName(getPosition()).equals("=")) {
                    writeOnFile(getMapeamento().writeOperator(getName(getPosition()), getName(getPosition()+1)));
                    if (isName(getPosition()+1)>0) {
                        writeOnFile("\"");
                        setPosition(2);
                        writeString(false);
                    }
                    else if (getMapeamento().isNameDefine(getName(getPosition()+1))) {
                        writeOnFile(getMapeamento().getDefineText(getName(getPosition()+1)));
                        setPosition(3);
                    }
                    else if (isNameList(getName(getPosition()+1))) {
                        writeOnFile(getName(getPosition()+1));
                        setPosition(3);
                    }
                    else if (getMapeamento().isNameFunction(getName(getPosition()+1))) {
                        checkParameters(getName(getPosition()+1), 3);
                    }
                    else {
                        setErrorType("estrutura");
                        errorFunction(getLine(getPosition()+1),"23 - Expressão com valor inválido.");
                    }
                    enableComparison = true;
                }
                else {
                    setErrorType("estrutura");
                    errorFunction(getLine(getPosition()),"27 - Falta operador para comparação.");
                }
            }
            else if (getName(getPosition()).equals("(")) {
                writeOnFile("(");
                setBracketsCounter(1);
                setPosition(1);
            }
            else if (getName(getPosition()).equals("e") || getName(getPosition()).equals("ou")) {
                System.out.println("Entrei no E");
                if (enableComparison) {
                    writeOnFile(getMapeamento().writeRelationalOperator(getName(getPosition())));
                    enableComparison = false;
                    setPosition(1);
                }
                else {
                    setErrorType("estrutura");
                    errorFunction(getLine(getPosition()),"28 - Confira a expressão anterior.");
                }
            }
            else if (getName(getPosition()).equals("nao")) {
                if (!enableComparison) {
                    writeOnFile(getMapeamento().writeRelationalOperator(getName(getPosition())));
                    enableComparison = false;
                    setPosition(1);
                }
                else {
                    setErrorType("estrutura");
                    errorFunction(getLine(getPosition()),"28 - Confira a expressão anterior.");
                }
            }
            else if (sintatico.isMathOperation(0, false, false, false)) {
                sintatico.writeMathOperation(0, false);
                if (sintatico.isLogicalOperator(getName(getPosition()))) {
                    writeNumberOperand();
                    setPosition(1);
                    if (sintatico.isMathOperation(0, true, false, false)) {
                        sintatico.writeMathOperation(0, true);
                        //setPosition(1);
                        enableComparison=true;
                    }
                    else {
                        setErrorType("estrutura");
                        errorFunction(getLine(getPosition()),"29 - Expressão matemática incorreta.");
                    }
                }
                else {
                    setErrorType("estrutura");
                    errorFunction(getLine(getPosition()),"30 - Operador inválido.");
                }
            }
            else if (isBoolean(getName(getPosition())) ||
                    isBooleanList(getName(getPosition())) ||
                    getMapeamento().isBooleanFunction(getName(getPosition())) || getMapeamento().isBooleanDefine(getName(getPosition()))) {
                if (getMapeamento().isBooleanFunction(getName(getPosition()))) {
                    checkParameters(getName(getPosition()), 2);
                }
                else if (getMapeamento().isBooleanDefine(getName(getPosition()))) {
                    writeOnFile(getMapeamento().getDefineText(getName(getPosition())));
                    setPosition(1);
                }
                else if (isBooleanList(getName(getPosition()))) {
                    writeOnFile(getName(getPosition()));
                    setPosition(1);
                }
                else {
                    writeBoolean(getName(getPosition()));
                    setPosition(1);
                }
            }
            else {
                setErrorType("estrutura");
                // Escreveu uma coisa diferente dentro do parênteses.
                errorFunction(getLine(getPosition()),"10 - Expressão inexistente.");
            }
        }
        /*
        //Tirei isso agora!
        else {
            setErrorType("sintaxe estrutura");
            // Fechou os parenteses e depois escreveu alguma coisa
            errorFunction(getLine(getPosition()+1),"16 - Confira os parênteses.");
        }
        */
    }
    
    private void writeOnFile(String name) {
        sintatico.writeOnFile(name);
    }
    
    private int getPosition() {
        return sintatico.getPosition();
    }
    
    private void setPosition(int position) {
        sintatico.setPosition(getPosition() + position);
    }
    
    private int getBracketsCounter() {
        return sintatico.getBracketsCounter();
    }
    
    private void setBracketsCounter(int bc) {
        sintatico.setBracketsCounter(getBracketsCounter() + bc);
    }
    
    private String getName(int position) {
        return sintatico.getName(position);
    }
    
    private int getLine(int position) {
        return sintatico.getLine(position);
    }
    
    private int getListSize() {
        return sintatico.getFileListSize();
    }
    
    private boolean isNumberList(String name) {
        return sintatico.numberListContains(name);
    }
    
    private boolean isNameList(String name) {
        return sintatico.nameListContains(name);
    }
    
    private boolean isBooleanList(String name) {
        return sintatico.booleanListContains(name);
    }
    
    private boolean getError() {
        return sintatico.isError();
    }
    
    private void errorFunction(int line, String name) {
        sintatico.errorFunction(line, name);
    }
    
    private void wordTest() {
        sintatico.wordTest();
    }
    
    private void writeString(boolean declaring) {
        sintatico.writeString(declaring);
    }
    
    private void writeNumberOperand() {
        sintatico.writeNumberOperand();
    }
    
    private boolean isNumber(String name) {
        return sintatico.isNumber(name);
    }
    
    private int isName(int index) {
        return sintatico.isName(index);
    }
    
    private boolean isBoolean(String name) {
        return sintatico.isBoolean(name);
    }
    
    private void writeBoolean(String name) {
        sintatico.writeBoolean(name);
    }
    
    private String[] switchCondition() {
        return sintatico.getSwitchCondition();
    }
    
    private Mapeamento getMapeamento() {
        return sintatico.getMapeamento();
    }
    
    private void checkParameters(String functionName, int checkPosition) {
        sintatico.getCheckParameters(functionName, checkPosition);
    }

    private void setErrorType(String _errorType) {
        sintatico.setErrorType(_errorType);
    }
}
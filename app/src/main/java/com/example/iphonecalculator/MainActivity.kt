package com.example.iphonecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.iphonecalculator.ui.theme.MyApplicationTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import net.objecthunter.exp4j.ExpressionBuilder
import java.math.BigDecimal
import java.math.RoundingMode


fun String.toBigDecimal(): BigDecimal = BigDecimal(this)



data class IconData(val imageVector: ImageVector,
                    val description: String,
                    val color: Color,
                    val textColor: Color)

val calculations: Array<String> = arrayOf("-","+","/","*")
val numbers: Set<String> = (0..9).map { it.toString() }.toSet()

fun isConvertibleToInt(str: String): Boolean {
    return str.toIntOrNull() != null
}

fun String.chunkFromEnd(size: Int): List<String> {
    return this.reversed()
        .chunked(size)
        .map { it.reversed() }
        .reversed()
}

fun getExpressionList(expr: String) : MutableList<Any> {

    // Separating numbers and calculations in list
    val modifiedExpr = expr.replace("(-0)","0")
    var currentNumber = StringBuilder()
    var currentChangeSignCount = 0
    var expressionList : MutableList<Any> = mutableListOf()

    for ((i, c) in modifiedExpr.withIndex()) {
//         println("$i $c")
//         println(currentNumber)

        if (c.toString() in calculations) {
            val currentNumberStr = if (currentChangeSignCount % 2 ==0) {
                currentNumber.toString()
            } else {
                "-" + currentNumber.toString()
            }
            currentChangeSignCount = 0
            expressionList.add(currentNumberStr.toBigDecimal())
            expressionList.add(c)
            currentNumber.clear()

        } else if ( c.toChar() == 'c') {
            currentChangeSignCount++
        } else if ( isConvertibleToInt(c.toString()) || c=='.' ) {
            currentNumber.append(c)
            if ( (i+1)==modifiedExpr.count() ) {
                val currentNumberStr = if (currentChangeSignCount % 2 ==0) {
                    currentNumber.toString()
                } else {
                    "-" + currentNumber.toString()
                }
                currentChangeSignCount = 0
                expressionList.add(currentNumberStr.toBigDecimal())
            }
        } else if ( (i+1)==modifiedExpr.count() && currentNumber.toString()!="" ) {
            val currentNumberStr = if (currentChangeSignCount % 2 ==0) {
                currentNumber.toString()
            } else {
                "-" + currentNumber.toString()
            }
            currentChangeSignCount = 0
            expressionList.add(currentNumberStr.toBigDecimal())
        }
//         println("-".repeat(20))
    }

    return expressionList
}


fun calculate(text: String, expression: String) : List<String> {
    val expressionList = getExpressionList(expression)

    val lastCalculation = expressionList.filter { it.toString() in calculations }.last()
    val lastNumber = expressionList.filter { it.toString() !in calculations }.last()
    val action = "$lastCalculation$lastNumber"

    if (expressionList.size==0) {
        return listOf(text, expression)
    } else{
        val cleanExpression = expressionList.joinToString("")
        val result = ExpressionBuilder(cleanExpression).build().evaluate()
            .toBigDecimal().setScale(10, RoundingMode.HALF_UP).stripTrailingZeros().toString()
        val cleanResult = if (".0"==result.takeLast(2)) {
            result.dropLast(2)
        } else {result}

        val textResult = if (cleanResult.toDouble().toString().takeLast(2)==".0") {
            cleanResult.toDouble().toString().dropLast(2)
        } else {cleanResult.toDouble().toString()}
        val expressionResult = cleanResult.toBigDecimal().toPlainString().replace("-","cs")


        return listOf(textResult, expressionResult, action)
    }
}



class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserProfilePreview()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun UserProfilePreview() {
    MyApplicationTheme {
        val buttonSize = 90.dp // different dp for different screen!
        var text by remember { mutableStateOf("0") }
        var expression by remember { mutableStateOf("0")}
        var lastAction by remember { mutableStateOf("")}
        var cMode by remember { mutableStateOf(false) }
        var selectedButton by remember {mutableStateOf("")}
        val buttons = listOf(
            if (cMode==false) {
                IconData(ImageVector.vectorResource(id = R.drawable.ac_path), "AC", Color(0xFFD4D4D2), Color.Black)
            } else {
                IconData(ImageVector.vectorResource(id = R.drawable.c_path), "C", Color(0xFFD4D4D2), Color.Black)
            },
            IconData(ImageVector.vectorResource(id = R.drawable.changesign), "changesign", Color(0xFFD4D4D2), Color.Black),
            IconData(ImageVector.vectorResource(id = R.drawable.percent), "percent", Color(0xFFD4D4D2), Color.Black),
            IconData(ImageVector.vectorResource(id = R.drawable.division), "/", Color(0xFFFF9500), Color.White), //WARNING: Find the division vector image!

            IconData(ImageVector.vectorResource(id = R.drawable.seven), "7", Color(0xFF333333), Color.White),
            IconData(ImageVector.vectorResource(id = R.drawable.eight), "8", Color(0xFF333333), Color.White),
            IconData(ImageVector.vectorResource(id = R.drawable.nine), "9", Color(0xFF333333), Color.White),
            IconData(Icons.Default.Close, "*", Color(0xFFFF9500), Color.White),

            IconData(ImageVector.vectorResource(id = R.drawable.four), "4", Color(0xFF333333), Color.White),
            IconData(ImageVector.vectorResource(id = R.drawable.five), "5", Color(0xFF333333), Color.White),
            IconData(ImageVector.vectorResource(id = R.drawable.six), "6", Color(0xFF333333), Color.White),
            IconData(Icons.Default.Remove, "-", Color(0xFFFF9500), Color.White),

            IconData(ImageVector.vectorResource(id = R.drawable.one), "1", Color(0xFF333333), Color.White),
            IconData(ImageVector.vectorResource(id = R.drawable.two), "2", Color(0xFF333333), Color.White),
            IconData(ImageVector.vectorResource(id = R.drawable.three), "3", Color(0xFF333333), Color.White),
            IconData(Icons.Default.Add, "+", Color(0xFFFF9500), Color.White),

            IconData(ImageVector.vectorResource(id = R.drawable.zero), "0", Color(0xFF333333), Color.White),
            IconData(ImageVector.vectorResource(id = R.drawable.dot), ",", Color(0xFF333333), Color.White),
            IconData(Icons.Default.DragHandle, "=", Color(0xFFFF9500), Color.White)

        )
        // General box
        Box (modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {

            Column (
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {

                // UI Text field
                Row (
                    modifier = Modifier
                        .align(Alignment.End)
                        .background(Color.Black)

                ) // Temporal column to stack textfields of expression and text
                { Column {
//                    // Temporal UI to see expression
//                    TextField(
//                        value = expression,
//                        onValueChange = { expression = it },
//                        modifier= Modifier
//                            .background(Color.Black)
//                            .border(0.dp, Color.Transparent)
//                            .fillMaxWidth()
//                        ,
//                        colors = TextFieldDefaults.colors(
//                            focusedTextColor = Color.White,
//                            unfocusedTextColor = Color.White,
//                            focusedContainerColor = Color.Black,
//
//                            //setting the text field background when it is unfocused or initial state
//                            unfocusedContainerColor = Color.Black,
//                            focusedIndicatorColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            disabledIndicatorColor = Color.Transparent,
//
//                            ),
//                        textStyle = TextStyle(
//                            fontSize = 30.sp,
//                            textAlign = TextAlign.End
//                        ),
//                        // Optional: If you want to remove the shape outline
//                        shape = RoundedCornerShape(0.dp)
//                    )

                    // Main UI text field
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier= Modifier
                            .background(Color.Black)
                            .border(0.dp, Color.Transparent)
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Black,

                            //setting the text field background when it is unfocused or initial state
                            unfocusedContainerColor = Color.Black,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,

                            ),
                        textStyle = TextStyle(
                            fontSize = 60.sp,
                            textAlign = TextAlign.End
                        ),
                        // Optional: If you want to remove the shape outline
                        shape = RoundedCornerShape(0.dp)
                    )
                }

                }

                // UI Buttons
                FlowRow {
                    buttons.forEach { buttonData ->
                        Button(
                            onClick = {
                                if (isConvertibleToInt(buttonData.description)) {
                                    cMode = true
                                    selectedButton = ""

                                    var tempText = text.replace(",","")

                                    if (tempText.replace(",","")
                                        .replace(".","").length < 9
                                        || expression.takeLast(1) in calculations) {

                                        if ((tempText=="0" && expression=="") || expression.last().toString() in calculations) {
                                            tempText = buttonData.description
                                            expression += buttonData.description
                                        } else if ( ((tempText.last().toString() == "," ) && (text[text.length - 2].toString() in numbers) )
                                            || tempText.last().toString() in numbers
                                            || (tempText.takeLast(2).first().toString() in numbers && text.last().toString() == "."))
                                        {
                                            if (tempText != "0" && tempText != "-0") {
                                                tempText += buttonData.description
                                                expression += buttonData.description
                                            } else {
                                                if (tempText.first().toString()=="-") {
                                                    tempText = "-" + buttonData.description
                                                    expression = expression.replace("(-0)","cs") + buttonData.description
                                                } else {
                                                    tempText = buttonData.description
                                                    expression = expression.replace("(0)","") + buttonData.description
                                                }

                                            }

                                        }
                                    }
                                    text = tempText.replace(",","").toBigDecimal()
                                        .setScale(10, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                                        .split(".").first().chunkFromEnd(3).joinToString(",")

                                } else if (buttonData.description == "AC") {
                                    expression = "0"
                                    text = "0"
                                    lastAction = ""
                                } else if (buttonData.description in calculations) {
                                    selectedButton = buttonData.description
                                    val expressionList = getExpressionList(expression)
                                    val calculationsAmount = expressionList.filter { it.toString() in calculations }.size
                                    val numberAmount = expressionList.filter { it.toString() !in calculations }.size
                                    if (calculationsAmount>=1 && numberAmount>=2) {
                                        val result = calculate(text, expression)
                                        selectedButton = ""
                                        text = result.first()
                                        expression = result[1]
                                        lastAction = result.last()

                                    }

                                    if (expression.last().toString() !in calculations) {
                                        expression += buttonData.description
                                        text = text
                                    } else {
                                        expression = expression.dropLast(1) + buttonData.description
                                        text = text
                                    }

                                } else if (buttonData.description=="changesign") {
                                    if (expression.takeLast(1) in calculations) {
                                        text = "-0"
                                        expression += "(-0)"

                                    } else {
                                        if (expression.takeLast(4)=="(-0)") {
                                            expression = expression.replace("(-0)","")
                                        } else {
                                            expression+="cs"
                                            if (text.first().toString() == "-") {
                                                text = text.drop(1)
                                            } else {
                                                text = "-" + text
                                            }
                                        }
                                    }
                                } else if (buttonData.description=="percent") {
                                    if (expression!="0" && text!="-0") {
                                        val tempText = (text.toDouble() / 100.0).toString()
                                        text = if (".0"==tempText.takeLast(2)) {
                                            tempText.dropLast(2) }
                                        else {tempText}

                                        var expressionList = getExpressionList(expression)
                                        val lastNumber = (expressionList
                                            .filter { it is BigDecimal }
                                            .takeLast(1)
                                            .first() as BigDecimal).toPlainString().toDouble()
                                        val previousExpressions = if (expressionList.last().toString() !in calculations) {
                                            expressionList.take(expressionList.size-1).joinToString("")
                                        } else {expressionList.joinToString("")}

                                        expression = previousExpressions + (lastNumber / 100.0)
                                            .toBigDecimal().toPlainString()
                                    } else {
                                        text = "0"
                                        expression = "0"
                                    }
                                } else if ( buttonData.description=="=" && (getExpressionList(expression).size>1 || lastAction!="") ) {

                                    if (getExpressionList(expression).size==1 && lastAction!="") {
                                        expression += lastAction
                                        val result = calculate(text, expression)
                                        selectedButton = ""
                                        text = result.first()
                                        expression = result[1]
                                        lastAction = result.last()
                                    } else if (getExpressionList(expression).size==2) {
                                        lastAction = getExpressionList(expression).last().toString() + getExpressionList(expression).first().toString()
                                        val tempExpression = expression.filterNot { it.toString() in calculations } + lastAction
                                        val result = calculate(text, tempExpression)
                                        selectedButton = ""
                                        text = result.first()
                                        expression = result[1]
                                        lastAction = result.last()
                                    } else if (getExpressionList(expression).size==3) {
                                        val result = calculate(text, expression)
                                        selectedButton = ""
                                        text = result.first()
                                        expression = result[1]
                                        lastAction = result.last()
                                    }

                                } else if (buttonData.description==",") {
                                    if (expression.takeLast(1) in calculations) {
                                        text = "0."
                                        expression += "0."

                                    } else {
                                        if ("." !in expression.split(*calculations).last()) {
                                            text += "."
                                            expression += "."
                                        }
                                    }
                                } else if (buttonData.description=="C") {
                                    // Implemented C->AC, when number+calculation
                                    // WRONG!!! -> C being as AC when only one number in expression
                                    cMode = false

                                    val expressionList = getExpressionList(expression)

                                    if (expressionList.size==1) {
                                        expression = "0"
                                        text = "0"
                                        lastAction = ""
                                    } else if (expressionList.last().toString() in calculations) {
                                        text = "0"
                                        expression = expression
                                    } else if (expressionList.last().toString().last().toString() in numbers) {
                                        text = "0"
                                        expression = expressionList.dropLast(1).joinToString("")
                                    }
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (buttonData.description==selectedButton) {
                                    buttonData.textColor
                                } else {buttonData.color},
                                contentColor = if (buttonData.description==selectedButton) {
                                    buttonData.color
                                } else {buttonData.textColor}
                            ),
                            modifier = Modifier
                                .padding(6.dp)
                                .then(
                                    if (buttonData.description == "0") {
                                        Modifier
                                            .width(buttonSize*2+11.dp)
                                            .height(buttonSize)
                                    } else {
                                        Modifier.size(buttonSize)
                                    }
                                )


                        ) {
                            Icon(imageVector = buttonData.imageVector,
                                contentDescription = buttonData.description,
                                tint = if (buttonData.description==selectedButton) {
                                    buttonData.color
                                } else {buttonData.textColor},
                                modifier = Modifier
                                    .size(300.dp))

                        }

                    }
                }
            }
        }

    }
}
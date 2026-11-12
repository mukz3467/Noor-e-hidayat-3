package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.NisabType
import com.example.data.model.ZakatCalculationInput
import com.example.data.model.ZakatResult
import com.example.data.repository.MediaAndLearningData
import com.example.localization.AppLocalization
import com.example.ui.components.NoorTopBar
import java.util.Locale

@Composable
fun ZakatScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    var cash by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var goldGrams by remember { mutableStateOf("") }
    var goldPrice by remember { mutableStateOf("75") } // Default USD/g
    var silverGrams by remember { mutableStateOf("") }
    var silverPrice by remember { mutableStateOf("0.95") } // Default USD/g
    var inventory by remember { mutableStateOf("") }
    var receivables by remember { mutableStateOf("") }
    var investments by remember { mutableStateOf("") }
    var liabilities by remember { mutableStateOf("") }
    var selectedNisab by remember { mutableStateOf(NisabType.SILVER) }
    var currency by remember { mutableStateOf("USD") }

    var calculationResult by remember { mutableStateOf<ZakatResult?>(null) }

    Scaffold(
        topBar = {
            NoorTopBar(
                title = AppLocalization.get("zakat", language),
                language = language,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Scholar Disclaimer
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = AppLocalization.get("zakat_disclaimer", language),
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nisab Standard Switcher (Silver vs Gold)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Nisab Standard Threshold",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = selectedNisab == NisabType.SILVER,
                            onClick = { selectedNisab = NisabType.SILVER },
                            label = { Text("Silver Nisab (612.36g / 52.5 Tola) - Recommended") },
                            modifier = Modifier.testTag("nisab_silver_chip")
                        )
                    }
                    FilterChip(
                        selected = selectedNisab == NisabType.GOLD,
                        onClick = { selectedNisab = NisabType.GOLD },
                        label = { Text("Gold Nisab (87.48g / 7.5 Tola)") },
                        modifier = Modifier.testTag("nisab_gold_chip")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Fields
            Text(
                text = "Zakatable Assets",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            ZakatInputField("Cash on Hand", cash, { cash = it }, "0.00", "cash_input")
            ZakatInputField("Bank Accounts & Savings", bank, { bank = it }, "0.00", "bank_input")
            ZakatInputField("Gold (in Grams)", goldGrams, { goldGrams = it }, "0.0", "gold_input")
            ZakatInputField("Silver (in Grams)", silverGrams, { silverGrams = it }, "0.0", "silver_input")
            ZakatInputField("Business Inventory / Merchandise", inventory, { inventory = it }, "0.00", "inventory_input")
            ZakatInputField("Receivables & Debts Owed to You", receivables, { receivables = it }, "0.00", "receivables_input")
            ZakatInputField("Stocks, Mutual Funds & Other Assets", investments, { investments = it }, "0.00", "investments_input")

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Deductible Liabilities & Immediate Debts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))

            ZakatInputField("Due Debts / Pending Bills", liabilities, { liabilities = it }, "0.00", "liabilities_input")

            Spacer(modifier = Modifier.height(18.dp))

            // Calculate Button
            Button(
                onClick = {
                    val input = ZakatCalculationInput(
                        cashOnHand = cash.toDoubleOrNull() ?: 0.0,
                        bankBalance = bank.toDoubleOrNull() ?: 0.0,
                        goldGrams = goldGrams.toDoubleOrNull() ?: 0.0,
                        goldGramPrice = goldPrice.toDoubleOrNull() ?: 75.0,
                        silverGrams = silverGrams.toDoubleOrNull() ?: 0.0,
                        silverGramPrice = silverPrice.toDoubleOrNull() ?: 0.95,
                        businessInventory = inventory.toDoubleOrNull() ?: 0.0,
                        receivables = receivables.toDoubleOrNull() ?: 0.0,
                        otherInvestments = investments.toDoubleOrNull() ?: 0.0,
                        debtsAndLiabilities = liabilities.toDoubleOrNull() ?: 0.0,
                        selectedNisabType = selectedNisab,
                        currency = currency
                    )
                    calculationResult = MediaAndLearningData.calculateZakat(input)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calculate_zakat_button")
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppLocalization.get("calculate", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Calculation Results
            if (calculationResult != null) {
                val res = calculationResult!!
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Calculation Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ResultLine("Total Gross Assets:", String.format(Locale.US, "%.2f %s", res.totalAssets, res.currency))
                        ResultLine("Deductible Liabilities:", String.format(Locale.US, "- %.2f %s", res.deductibleLiabilities, res.currency))
                        ResultLine("Net Zakatable Wealth:", String.format(Locale.US, "%.2f %s", res.netZakatableWealth, res.currency))
                        ResultLine("Nisab Threshold:", String.format(Locale.US, "%.2f %s", res.nisabThreshold, res.currency))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Zakat Due (2.5%):",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (res.isEligible) String.format(Locale.US, "%.2f %s", res.zakatPayable, res.currency) else "Below Nisab (No Zakat Due)",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (res.isEligible) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZakatInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag(testTag)
    )
}

@Composable
private fun ResultLine(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}

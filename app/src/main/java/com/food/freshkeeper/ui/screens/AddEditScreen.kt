package com.food.freshkeeper.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import androidx.compose.foundation.BorderStroke
import com.food.freshkeeper.FoodViewModel
import com.food.freshkeeper.data.FoodDataPresets
import com.food.freshkeeper.data.FoodItem
import com.food.freshkeeper.data.QuantityHelper
import com.food.freshkeeper.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ShelfLifeUnit(val label: String, val multiplier: Int) {
    DAY("天", 1),
    WEEK("周", 7),
    MONTH("月", 30),
    YEAR("年", 365)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    navController: NavHostController,
    viewModel: FoodViewModel,
    foodId: Long? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isEditMode = foodId != null && foodId > 0

    // 编辑已有食品时加载原数据
    val existingFood by if (isEditMode) {
        viewModel.getFoodById(foodId!!).collectAsState(initial = null)
    } else {
        remember { mutableStateOf<FoodItem?>(null) }
    }

    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("水果") }
    var iconEmoji by remember { mutableStateOf("🍎") }
    var imageUriString by remember { mutableStateOf<String?>(null) }
    var selectedLocation by remember { mutableStateOf("冷藏室 🧊") }

    // 保质期：数字输入与单位选择 (天 / 周 / 月 / 年)
    var shelfLifeNumberInput by remember { mutableStateOf("7") }
    var selectedUnit by remember { mutableStateOf(ShelfLifeUnit.DAY) }

    val defaultReminderDays by viewModel.defaultReminderDays.collectAsState()
    var productionDateMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // 物品数量：左侧数字输入 + 右侧 select 下拉单选单位
    var quantityNumberInput by remember { mutableStateOf("1") }
    var selectedQuantityUnit by remember { mutableStateOf("份") }
    var quantityUnitMenuExpanded by remember { mutableStateOf(false) }

    // 编辑已有食材时的快捷消灭数量选择
    var consumeCountInEdit by remember { androidx.compose.runtime.mutableIntStateOf(1) }

    var notes by remember { mutableStateOf("") }
    var reminderDaysBefore by remember(defaultReminderDays) { mutableIntStateOf(defaultReminderDays) }

    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = remember(
        name, selectedCategory, iconEmoji, imageUriString,
        selectedLocation, shelfLifeNumberInput, selectedUnit, quantityNumberInput, selectedQuantityUnit, notes
    ) {
        !isEditMode && (
            name.isNotBlank() ||
            selectedCategory != "水果" ||
            iconEmoji != "🍎" ||
            imageUriString != null ||
            selectedLocation != "冷藏室 🧊" ||
            shelfLifeNumberInput != "7" ||
            selectedUnit != ShelfLifeUnit.DAY ||
            quantityNumberInput != "1" ||
            selectedQuantityUnit != "份" ||
            notes.isNotBlank()
        )
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showDiscardDialog = true
    }

    // 当获取到已存数据时填入
    LaunchedEffect(existingFood) {
        existingFood?.let { food ->
            name = food.name
            selectedCategory = food.category
            iconEmoji = food.iconEmoji
            imageUriString = food.imageUri
            selectedLocation = food.location
            val totalDays = food.shelfLifeDays
            when {
                totalDays >= 365 && totalDays % 365 == 0 -> {
                    shelfLifeNumberInput = (totalDays / 365).toString()
                    selectedUnit = ShelfLifeUnit.YEAR
                }
                totalDays >= 30 && totalDays % 30 == 0 -> {
                    shelfLifeNumberInput = (totalDays / 30).toString()
                    selectedUnit = ShelfLifeUnit.MONTH
                }
                totalDays >= 7 && totalDays % 7 == 0 -> {
                    shelfLifeNumberInput = (totalDays / 7).toString()
                    selectedUnit = ShelfLifeUnit.WEEK
                }
                else -> {
                    shelfLifeNumberInput = totalDays.toString()
                    selectedUnit = ShelfLifeUnit.DAY
                }
            }
            productionDateMs = food.productionDateMs
            val parsedQty = QuantityHelper.parse(food.quantity)
            quantityNumberInput = (parsedQty.count ?: 1).toString()
            selectedQuantityUnit = parsedQty.unit
            consumeCountInEdit = 1
            notes = food.notes
            reminderDaysBefore = food.reminderDaysBefore
        }
    }

    val parsedNumber = shelfLifeNumberInput.toIntOrNull() ?: 1
    val calculatedTotalDays = (parsedNumber * selectedUnit.multiplier).coerceAtLeast(1)
    val calculatedExpiryDateMs = productionDateMs + calculatedTotalDays.toLong() * 86400000L

    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    val expiryDateStr = dateFormat.format(Date(calculatedExpiryDateMs))

    var showPhotoSourceSheet by remember { mutableStateOf(false) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val localPath = viewModel.saveImageToInternalStorage(it)
                imageUriString = localPath ?: it.toString()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraFile != null) {
            coroutineScope.launch {
                val savedPath = viewModel.saveCapturedPhotoToStorage(tempCameraFile!!)
                if (savedPath != null) {
                    imageUriString = savedPath
                } else if (tempCameraUri != null) {
                    imageUriString = tempCameraUri.toString()
                }
            }
        }
    }

    fun launchCamera() {
        try {
            val cacheDir = context.cacheDir
            val imagesDir = File(cacheDir, "camera_temp").apply { if (!exists()) mkdirs() }
            val file = File(imagesDir, "temp_photo_${System.currentTimeMillis()}.jpg")
            tempCameraFile = file
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "无法启动相机: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "需要相机权限拍摄食材照片", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestCameraAndLaunch() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cal = Calendar.getInstance().apply { timeInMillis = productionDateMs }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newCal = Calendar.getInstance()
            newCal.set(year, month, dayOfMonth, 0, 0, 0)
            productionDateMs = newCal.timeInMillis
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "编辑食材" else "存入新食材",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) {
                            showDiscardDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val foodToSave = FoodItem(
                                    id = if (isEditMode) foodId!! else 0L,
                                    name = name.trim(),
                                    category = selectedCategory,
                                    iconEmoji = iconEmoji,
                                    location = selectedLocation,
                                    productionDateMs = productionDateMs,
                                    shelfLifeDays = calculatedTotalDays,
                                    expiryDateMs = calculatedExpiryDateMs,
                                    quantity = "${quantityNumberInput.ifBlank { "1" }}$selectedQuantityUnit".trim(),
                                    notes = notes.trim(),
                                    imageUri = imageUriString,
                                    reminderDaysBefore = reminderDaysBefore,
                                    isConsumed = existingFood?.isConsumed ?: false,
                                    consumedAtMs = existingFood?.consumedAtMs,
                                    isDeleted = existingFood?.isDeleted ?: false,
                                    deletedAtMs = existingFood?.deletedAtMs
                                )
                                if (isEditMode) {
                                    viewModel.updateFood(foodToSave)
                                } else {
                                    viewModel.addFood(foodToSave)
                                }
                                navController.popBackStack()
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. 快捷模版选择（仅在添加模式下展示）
            if (!isEditMode) {
                Column {
                    Text(
                        text = "快捷预设",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FoodDataPresets.PRESET_ITEMS.forEach { preset ->
                            SuggestionChip(
                                onClick = {
                                    name = preset.name
                                    selectedCategory = preset.category
                                    iconEmoji = preset.iconEmoji
                                    selectedLocation = preset.location
                                    shelfLifeNumberInput = preset.defaultShelfLifeDays.toString()
                                    selectedUnit = ShelfLifeUnit.DAY
                                    val parsedPreset = QuantityHelper.parse(preset.defaultQuantity)
                                    quantityNumberInput = (parsedPreset.count ?: 1).toString()
                                    selectedQuantityUnit = parsedPreset.unit
                                    notes = preset.storageTip
                                },
                                label = {
                                    Text("${preset.iconEmoji} ${preset.name}", fontSize = 12.sp)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }

            // 2. 食材照片上传与 Emoji 选择
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("食材照片与图标", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        if (imageUriString != null) {
                            TextButton(
                                onClick = { imageUriString = null },
                                colors = ButtonDefaults.textButtonColors(contentColor = UrgentRed)
                            ) {
                                Text("移除照片", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(FreshGreenLight.copy(alpha = 0.6f))
                                .clickable { showPhotoSourceSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!imageUriString.isNullOrBlank()) {
                                AsyncImage(
                                    model = imageUriString,
                                    contentDescription = "食材照片",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "上传照片", tint = FreshGreenDark, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("上传照片", fontSize = 10.sp, color = FreshGreenDark, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (imageUriString != null) "已上传实物照片 ✨" else "点击左侧上传食材照片",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "支持拍照或从相册选择",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = { showPhotoSourceSheet = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(if (imageUriString != null) "更换照片 📷" else "拍照/相册 📷", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("或选用生动 Emoji 图标:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("🍎", "🍓", "🥦", "🥬", "🥩", "🍗", "🥛", "🧀", "🍞", "🍰", "🥚", "🦐", "🍱", "🧃", "🍫", "🍅", "🥑", "🍣").forEach { emoji ->
                            val isSelected = iconEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) FreshGreenContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(1.dp, FreshGreenPrimary, RoundedCornerShape(10.dp))
                                        else Modifier
                                    )
                                    .clickable { iconEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            // 3. 食品名称与基本属性
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("食材名称与存放区域", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("食材名称 (必填)", fontSize = 13.sp) },
                        placeholder = { Text("例如：鲜草莓、全脂牛奶") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            focusedBorderColor = FreshGreenPrimary,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("食品类别", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FoodDataPresets.CATEGORIES.filter { it.name != "全部" }.forEach { cat ->
                            val isSelected = selectedCategory == cat.name
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategory = cat.name
                                    iconEmoji = cat.iconEmoji
                                    selectedLocation = cat.defaultLocation
                                    shelfLifeNumberInput = cat.defaultShelfLifeDays.toString()
                                    selectedUnit = ShelfLifeUnit.DAY
                                },
                                label = { Text("${cat.iconEmoji} ${cat.name}", fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FreshGreenPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.Transparent,
                                    selectedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("存放位置", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FoodDataPresets.LOCATIONS.forEach { loc ->
                            val isSelected = selectedLocation == loc
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedLocation = loc },
                                label = { Text(loc, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarmOrange,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.Transparent,
                                    selectedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }

            // 4. 保质期设定
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("保质期设置与到期计算", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    // 生产/购入日期选择
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { datePickerDialog.show() }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = FreshGreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("生产/购买日期", fontSize = 13.sp)
                        }
                        Text(
                            text = dateFormat.format(Date(productionDateMs)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = FreshGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("保质期时长:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = shelfLifeNumberInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 4) {
                                    shelfLifeNumberInput = input
                                }
                            },
                            placeholder = { Text("如: 7") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                focusedBorderColor = FreshGreenPrimary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ShelfLifeUnit.values().forEach { unit ->
                                val isSelected = selectedUnit == unit
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedUnit = unit },
                                    label = { Text(unit.label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FreshGreenPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = Color.Transparent,
                                        selectedBorderColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("快速预设时长:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair("2天", Pair("2", ShelfLifeUnit.DAY)),
                            Pair("3天", Pair("3", ShelfLifeUnit.DAY)),
                            Pair("5天", Pair("5", ShelfLifeUnit.DAY)),
                            Pair("7天", Pair("7", ShelfLifeUnit.DAY)),
                            Pair("15天", Pair("15", ShelfLifeUnit.DAY)),
                            Pair("1周", Pair("1", ShelfLifeUnit.WEEK)),
                            Pair("2周", Pair("2", ShelfLifeUnit.WEEK)),
                            Pair("1个月", Pair("1", ShelfLifeUnit.MONTH)),
                            Pair("3个月", Pair("3", ShelfLifeUnit.MONTH)),
                            Pair("6个月", Pair("6", ShelfLifeUnit.MONTH)),
                            Pair("1年", Pair("1", ShelfLifeUnit.YEAR))
                        ).forEach { (label, config) ->
                            SuggestionChip(
                                onClick = {
                                    shelfLifeNumberInput = config.first
                                    selectedUnit = config.second
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 到期计算结果卡
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(UrgentRedBg)
                            .border(0.8.dp, UrgentRedBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("预计到期时间 (共 $calculatedTotalDays 天)", fontSize = 11.sp, color = UrgentRed)
                                Text(
                                    text = expiryDateStr,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UrgentRed
                                )
                            }
                            Text("⏳ 实时计算", fontSize = 11.sp, color = UrgentRed)
                        }
                    }
                }
            }

            // 5. 规格数量与备注
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("规格与数量", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("物品数量与单位:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityNumberInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 5) {
                                    quantityNumberInput = input
                                }
                            },
                            placeholder = { Text("数量 如: 1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.3f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                focusedBorderColor = FreshGreenPrimary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        // Select 样式的单位单选下拉菜单
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (quantityUnitMenuExpanded) FreshGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clickable { quantityUnitMenuExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedQuantityUnit,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "选择单位",
                                        tint = FreshGreenPrimary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = quantityUnitMenuExpanded,
                                onDismissRequest = { quantityUnitMenuExpanded = false },
                                modifier = Modifier
                                    .heightIn(max = 280.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                val unitsList = remember(selectedQuantityUnit) {
                                    if (selectedQuantityUnit !in QuantityHelper.ALL_UNITS) {
                                        listOf(selectedQuantityUnit) + QuantityHelper.ALL_UNITS
                                    } else {
                                        QuantityHelper.ALL_UNITS
                                    }
                                }
                                unitsList.forEach { unitItem ->
                                    val isSelected = selectedQuantityUnit == unitItem
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = unitItem,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) FreshGreenPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = FreshGreenPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedQuantityUnit = unitItem
                                            quantityUnitMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 编辑模式下直接增加一行快捷消灭操作（不用弹出界面）
                    val currentCount = quantityNumberInput.toIntOrNull() ?: 1
                    val isDiscrete = QuantityHelper.DISCRETE_UNITS.contains(selectedQuantityUnit) ||
                                     QuantityHelper.DISCRETE_UNITS.any { selectedQuantityUnit.startsWith(it) }

                    if (isEditMode && isDiscrete && currentCount >= 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FreshGreenLight.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, FreshGreenPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🍽️ 快捷消灭 (当前剩 $currentCount$selectedQuantityUnit)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FreshGreenPrimary
                                    )
                                    Text(
                                        text = "就地快速记录食用数量",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilledTonalIconButton(
                                        onClick = { if (consumeCountInEdit > 1) consumeCountInEdit-- },
                                        enabled = consumeCountInEdit > 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "减少", modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = "$consumeCountInEdit$selectedQuantityUnit",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    FilledTonalIconButton(
                                        onClick = { if (consumeCountInEdit < currentCount) consumeCountInEdit++ },
                                        enabled = consumeCountInEdit < currentCount,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "增加", modifier = Modifier.size(16.dp))
                                    }

                                    Button(
                                        onClick = {
                                            val remain = currentCount - consumeCountInEdit
                                            if (remain <= 0) {
                                                existingFood?.let {
                                                    viewModel.markConsumed(it)
                                                    Toast.makeText(context, "太棒啦！已全部消灭【${it.name}】🎉", Toast.LENGTH_SHORT).show()
                                                }
                                                navController.popBackStack()
                                            } else {
                                                quantityNumberInput = remain.toString()
                                                existingFood?.let {
                                                    viewModel.confirmConsume(it, consumeCountInEdit)
                                                }
                                                Toast.makeText(context, "已消灭 $consumeCountInEdit$selectedQuantityUnit，剩余 $remain$selectedQuantityUnit 😋", Toast.LENGTH_SHORT).show()
                                                consumeCountInEdit = 1
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = if (consumeCountInEdit == currentCount) "全消灭" else "消灭",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("保存备注或烹饪计划", fontSize = 13.sp) },
                        placeholder = { Text("如：开封后48小时喝完、搭配吐司做早餐") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            focusedBorderColor = FreshGreenPrimary,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    text = "放弃录入？",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = "当前输入的内容尚未保存，返回后将丢失。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text(
                        text = "放弃录入",
                        color = UrgentRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false }
                ) {
                    Text(
                        text = "继续填写",
                        color = FreshGreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "选择食材照片",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoSourceSheet = false
                            requestCameraAndLaunch()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = FreshGreenPrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("拍照", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("调出相机现场拍摄新鲜食材实物照片", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoSourceSheet = false
                            galleryLauncher.launch("image/*")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = FreshGreenPrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("从相册选择", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("选择手机内已保存的食材照片", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (!imageUriString.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                imageUriString = null
                                showPhotoSourceSheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = UrgentRed, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("移除当前照片", color = UrgentRed, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("清空已选择的照片并使用 Emoji 图标", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


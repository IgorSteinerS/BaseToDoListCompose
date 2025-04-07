@file:OptIn(ExperimentalMaterial3Api::class)

package br.edu.satc.todolistcompose.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.edu.satc.todolistcompose.AppDatabase
import br.edu.satc.todolistcompose.TaskData
import br.edu.satc.todolistcompose.TaskEntity
import br.edu.satc.todolistcompose.ui.components.TaskCard
import kotlinx.coroutines.launch


@Preview(showBackground = true)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = db.taskDao()

    var showBottomSheet by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()

    var taskList by remember { mutableStateOf(listOf<TaskEntity>()) }

    // Carrega as tarefas do banco na inicialização
    LaunchedEffect(Unit) {
        taskList = dao.getAll()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("ToDoList UniSATC") },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nova tarefa") },
                icon = { Icon(Icons.Filled.Add, contentDescription = "") },
                onClick = { showBottomSheet = true }
            )
        }
    ) { innerPadding ->
        HomeContent(taskList, innerPadding)
        NewTask(showBottomSheet = showBottomSheet) { title, description ->
            scope.launch {
                val newTask = TaskEntity(title = title, description = description, complete = false)
                dao.insert(newTask)
                taskList = dao.getAll()
            }
            showBottomSheet = false
        }
    }
}

@Composable
fun HomeContent(tasks: List<TaskEntity>, innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .padding(top = innerPadding.calculateTopPadding())
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        for (task in tasks) {
            TaskCard(task.title, task.description, task.complete)
        }
    }
}


/**
 * NewTask abre uma janela estilo "modal". No Android conhecida por BottomSheet.
 * Aqui podemos "cadastrar uma nova Task".
 */
@Composable
fun NewTask(showBottomSheet: Boolean, onSave: (String, String) -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {},
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Título da tarefa") }
                )
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    label = { Text("Descrição da tarefa") }
                )
                Button(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onSave(taskTitle, taskDescription)
                        }
                    }
                ) {
                    Text("Salvar")
                }
            }
        }
    }
}

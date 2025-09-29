package com.example.contactsapp.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ContactDetailsScreen(
    contactId: Int,
    navController: NavController,
    viewModel: ContactsViewModel = viewModel()
) {
    val ctx = LocalContext.current
    var displayPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraPhotoUri != null) {
            displayPhotoUri = tempCameraPhotoUri
            tempCameraPhotoUri?.let { uri ->

                viewModel.updateContactImage(contactId, uri.toString())
            }
        } else if (!success) {
            Toast.makeText(ctx, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
        tempCameraPhotoUri = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            displayPhotoUri = it
            viewModel.updateContactImage(contactId, it.toString())
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempCameraPhotoUri = createImageUri(ctx)
            tempCameraPhotoUri?.let { uri ->
                cameraLauncher.launch(uri)
            } ?: Toast.makeText(ctx, "Could not create image file", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(ctx, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(contactId) {
        viewModel.getContactById(contactId)
    }

    val contact by viewModel.contactLiveData.observeAsState()


    LaunchedEffect(contact) {
        contact?.imageUrl?.let { urlString ->
            if (urlString.isNotBlank()) {
                try {
                    displayPhotoUri = Uri.parse(urlString)
                } catch (e: Exception) {
                    displayPhotoUri = null
                    println("Error parsing image URI: $urlString")
                }
            } else {
                displayPhotoUri = null
            }
        } ?: run {
            displayPhotoUri = null
        }
    }

    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onClickCamera = {
                showImageSourceDialog = false
                if (cameraPermissionState.status.isGranted) {
                    tempCameraPhotoUri = createImageUri(ctx)
                    tempCameraPhotoUri?.let { uri ->
                        cameraLauncher.launch(uri)
                    } ?: Toast.makeText(ctx, "Could not create image file", Toast.LENGTH_SHORT).show()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onClickGallery = {
                showImageSourceDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contact?.name ?: "Contact Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {

                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Contact")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))


            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showImageSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (displayPhotoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = displayPhotoUri,
                            onError = { error ->
                                println("Coil error: ${error.result.throwable.message}")
                                displayPhotoUri = null
                            }
                        ),
                        contentDescription = "Contact Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default Contact Photo",
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            contact?.let { currentContact ->
                Text(
                    text = currentContact.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                DetailItem(
                    icon = Icons.Outlined.Phone,
                    text = currentContact.phoneNumber,
                    contentDescription = "Phone Number"
                )

                if (currentContact.email.isNotBlank()) {
                    DetailItem(
                        icon = Icons.Outlined.Email,
                        text = currentContact.email,
                        contentDescription = "Email Address"
                    )
                }



                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionButton(
                        icon = Icons.Default.Call,
                        text = "Call",
                        onClick = { }
                    )
                    ActionButton(
                        icon = Icons.Outlined.Send,
                        text = "Message",
                        onClick = { }
                    )
                }
                Spacer(Modifier.height(16.dp))
            } ?: run {
                Spacer(Modifier.height(20.dp))
                if (viewModel.contactLiveData.value == null && contactId != 0) {
                    CircularProgressIndicator()
                    Text("Loading contact details...")
                } else {
                    Text("Contact not found.")
                }
            }
        }
    }
}

@Composable
private fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, contentDescription: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = text)
            Spacer(Modifier.width(8.dp))
            Text(text)
        }
    }
}


@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onClickCamera: () -> Unit,
    onClickGallery: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Change contact photo",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Select source:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onClickCamera,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Take Photo (Camera)")
                }
                Button(
                    onClick = onClickGallery,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose from Gallery")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
fun createImageUri(context: Context): Uri? {
    return try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timestamp}_"
        val storageDir = context.getExternalFilesDir("Pictures")

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }

        val imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error creating image file: ${e.message}", Toast.LENGTH_LONG).show()
        null
    }
}
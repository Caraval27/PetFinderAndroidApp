package com.example.petfinderapp.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.petfinderapp.R
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.presentation.viewModel.PetFinderVM
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PostDetailsScreen(
    petFinderVM: PetFinderVM,
    postId: String
) {
    val post = petFinderVM.post.collectAsState()
    val pagerState = rememberPagerState(pageCount = { post.value.images.size })

    LaunchedEffect(postId) {
        petFinderVM.initDetails(postId)
        petFinderVM.updateIsReturningFromDetails(true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) { page ->
            Image(
                painter = rememberAsyncImagePainter(model = post.value.images[page]),
                contentDescription = "Photo from post",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (pagerState.pageCount > 1) {
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = post.value.title,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        var timeText = ""
        if (post.value.time.isNotEmpty()) {
            timeText = LocalDateTime.parse(post.value.time).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        }

        Text(
            text = timeText,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when (post.value.postType) {
                PostType.Searching -> {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Searching for a pet",
                        tint = Color(0xFF1968A6)
                    )
                    Text(
                        text = PostType.Searching.toString(),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF1968A6)
                        )
                    )
                }
                PostType.Found -> {
                    Icon(
                        painter = painterResource(id = R.drawable.found_icon),
                        contentDescription = "Found a pet",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF1C7520)
                    )
                    Text(
                        text = PostType.Found.toString(),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF1C7520)
                        )
                    )
                }
                null -> {}
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color.LightGray
        )

        Text(
            text = "Animal details",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = buildString {
                append(post.value.animalType)
                if (post.value.breed.isNotEmpty()) {
                    append(", ${post.value.breed.joinToString(separator = ", ")}")
                }
                if (post.value.color.isNotEmpty()) {
                    append(", ${post.value.color.joinToString(separator = ", ")}")
                }
            },
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color.LightGray
        )

        Text(
            text = "Description",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = post.value.description,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color.LightGray
        )

        Text(
            text = "Posted by: ${post.value.userName}",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Contact: ${post.value.phoneNumber}",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}
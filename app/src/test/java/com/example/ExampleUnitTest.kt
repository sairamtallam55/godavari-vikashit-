package com.example

import com.example.data.HeritageRepository
import com.example.model.Model3DType
import com.example.ui.components.generateHeritageMesh
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testHeritageDestinationsCountAndIntegrity() {
    val destinations = HeritageRepository.destinations
    assertEquals(10, destinations.size)
    destinations.forEach { dest ->
      assertNotNull(dest.id)
      assertTrue(dest.nameEn.isNotBlank())
      assertTrue(dest.nameTe.isNotBlank())
      assertTrue(dest.nameHi.isNotBlank())
      assertTrue(dest.latitude > 0.0)
      assertTrue(dest.longitude > 0.0)
      assertTrue(dest.highlights.isNotEmpty())
    }
  }

  @Test
  fun test3DHeritageModelsMeshGeneration() {
    val models = HeritageRepository.model3DItems
    assertEquals(8, models.size)

    Model3DType.entries.forEach { type ->
      val mesh = generateHeritageMesh(type)
      assertTrue("Mesh $type should have vertices", mesh.vertices.isNotEmpty())
      assertTrue("Mesh $type should have edges", mesh.edges.isNotEmpty())
    }
  }

  @Test
  fun testVirtualToursCountAndStops() {
    val tours = HeritageRepository.virtualTours
    assertEquals(5, tours.size)
    tours.forEach { tour ->
      assertTrue(tour.stops.isNotEmpty())
      tour.stops.forEach { stop ->
        assertTrue(stop.stopNumber > 0)
        assertTrue(stop.keySight.isNotBlank())
      }
    }
  }

  @Test
  fun testCulturalArticlesAndExhibitions() {
    assertEquals(10, HeritageRepository.culturalArticles.size)
    assertEquals(10, HeritageRepository.exhibitionItems.size)
    assertEquals(20, HeritageRepository.heritageFaqs.size)
  }

  @Test
  fun testQuizQuestionsAndDifficulty() {
    val questions = HeritageRepository.quizQuestions
    assertEquals(15, questions.size)
    questions.forEach { q ->
      assertTrue(q.optionsEn.size >= 4)
      assertTrue(q.correctIndex in 0..3)
      assertTrue(q.explanationEn.isNotBlank())
    }

    val novice = questions.filter { it.difficulty == "Novice" }
    val scholar = questions.filter { it.difficulty == "Scholar" }
    assertTrue(novice.isNotEmpty())
    assertTrue(scholar.isNotEmpty())
  }
}

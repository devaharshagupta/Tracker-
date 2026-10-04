package com.example.data.entity

data class GitaShloka(
  val chapter: Int,
  val verse: Int,
  val sanskrit: String,
  val transliteration: String,
  val englishTranslation: String,
  val practicalTakeaway: String
)

object GitaShlokaRepository {
  val dailyShlokas = listOf(
    GitaShloka(
      chapter = 2,
      verse = 47,
      sanskrit = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥",
      transliteration = "karmaṇy evādhikāras te mā phaleṣu kadācana\nmā karma-phala-hetur bhūr mā te saṅgo 'stv akarmaṇi",
      englishTranslation = "You have a right to perform your prescribed duty, but you are not entitled to the fruits of action. Never consider yourself the cause of the results of your activities, and never be attached to not doing your duty.",
      practicalTakeaway = "Prepare for GATE and College exams with full dedication and tapasya, while offering the results to Krishna with an unburdened, peaceful mind."
    ),
    GitaShloka(
      chapter = 6,
      verse = 5,
      sanskrit = "उद्धरेदात्मनात्मानं नात्मानमवसादयेत्।\nआत्मैव ह्यात्मनो बन्धुरात्मैव रिपुरात्मनः॥",
      transliteration = "uddhared ātmanātmānaṁ nātmānam avasādayet\nātmaiva hy ātmano bandhur ātmaiva ripur ātmanaḥ",
      englishTranslation = "One must deliver oneself with the help of the mind, and not degrade oneself. The mind is the friend of the conditioned soul, and his enemy as well.",
      practicalTakeaway = "Discipline your mind through morning chanting and focused study blocks. A regulated mind is your best ally in academics and spiritual progress."
    ),
    GitaShloka(
      chapter = 6,
      verse = 17,
      sanskrit = "युक्ताहारविहारस्य युक्तचेष्टस्य कर्मसु।\nयुक्तस्वप्नावबोधस्य योगो भवति दुःखहा॥",
      transliteration = "yuktāhāra-vihārasya yukta-ceṣṭasya karmasu\nyukta-svapnāvabodhasya yogo bhavati duḥkha-hā",
      englishTranslation = "He who is regulated in his habits of eating, sleeping, recreation and work can mitigate all material pains by practicing the yoga system.",
      practicalTakeaway = "Balance your daily routine: wake up for Mangala/Japa, eat sanctified prasadam, attend college classes, and schedule deep GATE study blocks."
    ),
    GitaShloka(
      chapter = 18,
      verse = 78,
      sanskrit = "यत्र योगेश्वरः कृष्णो यत्र पार्थो धनुर्धरः।\nतत्र श्रीर्विजयो भूतिर्ध्रुवा नीतिर्मतिर्मम॥",
      transliteration = "yatra yogeśvaraḥ kṛṣṇo yatra pārtho dhanur-dharaḥ\ntatra śrīr vijayo bhūtir dhruvā nītir matir mama",
      englishTranslation = "Wherever there is Krishna, the master of all mystics, and wherever there is Arjuna, the supreme archer, there will also certainly be opulence, victory, extraordinary power, and morality.",
      practicalTakeaway = "Combine supreme spiritual devotion (Krishna) with skilled, diligent action (Arjuna's archery). Excellence in study is your offering of duty."
    ),
    GitaShloka(
      chapter = 9,
      verse = 27,
      sanskrit = "यत्करोषि यदश्नासि यज्जुहोषि ददासि यत्।\nयत्तपस्यसि कौन्तेय तत्कुरुष्व मदर्पणम्॥",
      transliteration = "yat karoṣi yad aśnāsi yaj juhoṣi dadāsi yat\nyat tapasyasi kaunteya tat kuruṣva mad-arpaṇam",
      englishTranslation = "Whatever you do, whatever you eat, whatever you offer or give away, and whatever austerities you perform—do that, O son of Kuntī, as an offering unto Me.",
      practicalTakeaway = "Study every concept, solve every equation, and honor your college responsibilities as a sincere offering of devotion."
    )
  )

  fun getTodayShloka(): GitaShloka {
    val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
    return dailyShlokas[dayOfYear % dailyShlokas.size]
  }
}

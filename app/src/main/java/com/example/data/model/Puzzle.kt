package com.example.data.model

enum class PuzzleCategory(val displayName: String, val description: String) {
    MATH("Sayısal", "Hızlı hesaplama, sayı dizileri ve aritmetik yetenek."),
    LOGIC("Mantıksal", "Problem çözme, analitik düşünme ve tümdengelim."),
    MEMORY("Hafıza", "Stroop testi, görsel matrisler ve sayı hatırlama."),
    WORD("Sözel", "Kelime ilişkileri, anagramlar ve sözcük dağarcığı.")
}

enum class PuzzleType {
    MULTIPLE_CHOICE,
    NUMERIC_INPUT,
    STROOP_COLOR,
    GRID_MEMORY,
    NUMBER_MEMORY,
    WORD_UNSCRAMBLE
}

data class Puzzle(
    val id: Int,
    val title: String,
    val category: PuzzleCategory,
    val type: PuzzleType,
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val hint: String,
    val extraData: String = "" // Used for color codes in Stroop, or sequence for Memory
)

object PuzzlesList {
    val puzzles: List<Puzzle> = listOf(
        // ==========================================
        // MATH (Levels 1, 5, 9, 13, 17, 21, 25, 29, 33, 37, 41, 45, 49)
        // ==========================================
        Puzzle(
            id = 1,
            title = "Katlanarak Artan Sayı Dizisi",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "Aşağıdaki sayı dizisinde her sayı kendisinden bir öncekinin 2 katıdır (×2). Kuralı takip ederek soru işareti (?) yerine gelecek sayıyı bulunuz:\n\n2 ➔ 4 ➔ 8 ➔ 16 ➔ ?",
            correctAnswer = "32",
            hint = "Her adımda sayı 2 ile çarpılmaktadır: 2×2=4, 4×2=8, 8×2=16. Dolayısıyla sıradaki sayı 16 × 2 = 32 olmalıdır."
        ),
        Puzzle(
            id = 5,
            title = "Aritmetik İşlem Önceliği",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "Matematikte işlem önceliği kuralına göre ÇARPMA işlemi TOPLAMA işleminden önce yapılır. Bu kurala göre aşağıdaki işlemin sonucunu hesaplayınız:\n\n15 + 4 × 3 = ?",
            correctAnswer = "27",
            hint = "Önce çarpma işlemi yapılır: 4 × 3 = 12. Ardından toplama yapılır: 15 + 12 = 27."
        ),
        Puzzle(
            id = 9,
            title = "Tersten Çözülen Elma Problemi",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "Bir manavdaki elmaların önce yarısı, ardından kalanların 3 fazlası satılınca geriye sepette 12 adet elma kalıyor. Başlangıçta sepette toplam kaç elma vardı?",
            correctAnswer = "30",
            hint = "Tersten hesaplama: Kalan 12 elmaya satılan 3 elmayı ekleyin (12 + 3 = 15). Bu miktar başlangıçtaki elmaların yarısı olduğuna göre sepetin tamamı 15 × 2 = 30 elmadır."
        ),
        Puzzle(
            id = 13,
            title = "Çarpım ve Fark İşlemi",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "Parantez içi çarpma işlemlerini hesaplayarak aralarındaki farkı bulunuz:\n\n(3 × 3 × 3) − (3 × 3) = ?",
            correctAnswer = "18",
            hint = "Önce parantez içi çarpımları hesaplayın: 3 × 3 × 3 = 27 ve 3 × 3 = 9. Ardından farkı alın: 27 − 9 = 18."
        ),
        Puzzle(
            id = 17,
            title = "Artan Farklı Sayı Dizisi",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "Bu dizide sayılar arasındaki artış miktarı her adımda 1 fazlalaşmaktadır (+2, +3, +4, +5...). Soru işareti (?) yerine gelecek sayıyı hesaplayınız:\n\n1 ➔ 3 ➔ 6 ➔ 10 ➔ 15 ➔ ?",
            correctAnswer = "21",
            hint = "Artış adımları: 1 (+2) = 3, 3 (+3) = 6, 6 (+4) = 10, 10 (+5) = 15. Bir sonraki artış +6 olmalıdır: 15 + 6 = 21."
        ),
        Puzzle(
            id = 21,
            title = "Bölme ve Çarpma Zinciri",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "İşlem önceliğine göre önce bölme ve çarpma işlemlerini yapıp ardından toplayınız:\n\n(88 ÷ 8) + (3 × 5) = ?",
            correctAnswer = "26",
            hint = "Önce bölmeyi yapın: 88 ÷ 8 = 11. Sonra çarpmayı yapın: 3 × 5 = 15. Son olarak toplayın: 11 + 15 = 26."
        ),
        Puzzle(
            id = 25,
            title = "Yüzde Hesaplama Zinciri",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "100 sayısının önce %20'si hesaplanıyor. Ardından elde edilen sonucun %50'si (yani yarısı) alınıyor. Nihai sonuç kaçtır?",
            correctAnswer = "10",
            hint = "100'ün %20'si = 20 eder. 20'nin %50'si (yani yarısı) ise 10 eder."
        ),
        Puzzle(
            id = 29,
            title = "Dokuzar Ritmik Artış",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "Dokuzar dokuzar ritmik artan aşağıdaki sayı dizisinde soru işareti (?) yerine hangi sayı gelmelidir?\n\n9 ➔ 18 ➔ 27 ➔ 36 ➔ ?",
            correctAnswer = "45",
            hint = "Sayılar 9'ar 9'ar artmaktadır (9'un katları). 36 + 9 = 45."
        ),
        Puzzle(
            id = 33,
            title = "İşlem Önceliği Uygulaması",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "Önce parantez içi çarpma ve bölme işlemlerini yapıp sonucunu hesaplayınız:\n\n(4 × 4) − (4 ÷ 4) = ?",
            correctAnswer = "15",
            hint = "Önce çarpma ve bölme yapılır: 4 × 4 = 16 ve 4 ÷ 4 = 1. Sonuç: 16 − 1 = 15."
        ),
        Puzzle(
            id = 37,
            title = "Adım Adım Artan Dizi",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "Her adımda artış miktarı 1 fazlalaşan (+1, +2, +3, +4...) aşağıdaki sayı dizisini tamamlayınız:\n\n10 ➔ 11 ➔ 13 ➔ 16 ➔ 20 ➔ ?",
            correctAnswer = "25",
            hint = "Farklar sırasıyla: +1, +2, +3, +4 şeklindedir. Sıradaki artış +5 olmalıdır: 20 + 5 = 25."
        ),
        Puzzle(
            id = 41,
            title = "Ortak Çarpan Pratik Hesabı",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "İşlem önceliğine dikkat ederek aşağıdaki fark işleminin sonucunu bulunuz:\n\n(9 × 9) − (9 × 8) = ?",
            correctAnswer = "9",
            hint = "9 × 9 = 81 ve 9 × 8 = 72. Farkı: 81 − 72 = 9. (Veya ortak 9 parantezinde: 9 × (9 - 8) = 9 × 1 = 9)."
        ),
        Puzzle(
            id = 45,
            title = "Artan Farklar Merdiveni",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "Sayılar arasındaki artışın her adımda 1 arttığı (+1, +2, +3, +4, +5, +6...) bu dizide soru işareti (?) yerine hangi sayı gelmelidir?\n\n1 ➔ 2 ➔ 4 ➔ 7 ➔ 11 ➔ 16 ➔ ?",
            correctAnswer = "22",
            hint = "Farklar sırasıyla: +1, +2, +3, +4, +5 şeklindedir. Sıradaki artış +6 olmalıdır: 16 + 6 = 22."
        ),
        Puzzle(
            id = 49,
            title = "Parantezli Karmaşık İşlem",
            category = PuzzleCategory.MATH,
            type = PuzzleType.NUMERIC_INPUT,
            question = "İşlem önceliğine göre önce parantez içini, ardından çarpmayı ve toplamayı yapınız:\n\n5 × (12 − 7) + 8 = ?",
            correctAnswer = "33",
            hint = "Parantez içi: 12 − 7 = 5. Çarpma: 5 × 5 = 25. Toplama: 25 + 8 = 33."
        ),

        // ==========================================
        // LOGIC (Levels 2, 6, 10, 14, 18, 22, 26, 30, 34, 38, 42, 46, 50)
        // ==========================================
        Puzzle(
            id = 2,
            title = "Güvercin Yuvası İlkesi",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Bir torbada 4 kırmızı, 5 mavi ve 6 yeşil bilye vardır. Işıkların kapalı olduğu karanlık bir odada torbadan en az kaç bilye çekilirse, KESİNLİKLE aynı renkten en az bir çift (2 adet) bilye çekilmiş olur?",
            options = listOf("3", "5", "4", "6"),
            correctAnswer = "4",
            hint = "Torbadaki 3 farklı renkten (kırmızı, mavi, yeşil) ilk 3 çekişte farklı renkler gelse bile, 4. çekilen bilye mutlaka önceki 3 renkten biriyle eşleşerek çift oluşturur."
        ),
        Puzzle(
            id = 6,
            title = "Saat Kadranı Akrep-Yelkovan",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Doğru çalışan analog bir saatin akrebi ile yelkovanı, öğlen 12:00'den gece 24:00'e kadar (12 saatlik sürede) tam olarak kaç kez üst üste gelir?",
            options = listOf("10", "12", "11", "13"),
            correctAnswer = "11",
            hint = "Akreple yelkovan her ~65.45 dakikada bir üst üste gelir. 11:00 ile 12:00 arasında ayrı bir kesişim olmaz; 12 saatlik turda tam 11 kez üst üste gelirler."
        ),
        Puzzle(
            id = 10,
            title = "Üç Kişilik Yarış Paradoksu",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Ali, Burak ve Can bir koşu yarışına katılmıştır.\nAli: 'Ben birinci olmadım.'\nBurak: 'Ben sonuncu olmadım.'\nCan: 'Ben birinci oldum.'\n\nBu üç kişiden YALNIZCA BİRİ DOĞRU söylüyorsa yarışı kim birinci bitirmiştir?",
            options = listOf("Can", "Ali", "Eşitlik", "Burak"),
            correctAnswer = "Burak",
            hint = "Eğer Burak birinci olursa: Ali doğru söyler ('ben birinci olmadım'), Burak yalan söyler ('sonuncu olmadım' diyerek yalan söyler çünkü 1. olmuştur), Can yalan söyler. Tam 1 kişi doğru söyler!"
        ),
        Puzzle(
            id = 14,
            title = "Üstel Büyüme ve Nilüferler",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Bir göletteki nilüfer yaprakları her gün kapladığı alanı tam 2 katına çıkarmaktadır. Göletin tamamı 48 günde tamamen kaplandığına göre, göletin tam YARISI kaçıncı günde kaplanmıştır?",
            options = listOf("24. Gün", "12. Gün", "46. Gün", "47. Gün"),
            correctAnswer = "47. Gün",
            hint = "Alan her gün 2 katına çıktığı için, 48. günde gölün tamamı doluysa bir gün öncesinde (47. günde) gölün tam yarısı doludur."
        ),
        Puzzle(
            id = 18,
            title = "Turnuva Eşleşme Kombinasyonu",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Bir satranç turnuvasına katılan 8 usta oyuncunun her biri birbiriyle tam olarak 1 maç yapacaktır. Turnuva boyunca toplam kaç maç oynanır?",
            options = listOf("28", "56", "64", "32"),
            correctAnswer = "28",
            hint = "Kombinasyon formülü: C(8,2) = (8 × 7) ÷ 2 = 28 maç oynanır."
        ),
        Puzzle(
            id = 22,
            title = "Monty Hall Seçim Olasılığı",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Önünüzde 3 kapı vardır; birinin arkasında araba, diğer ikisinde keçi vardır. Bir kapı seçtiniz. Sunucu kalan 2 kapıdan arkasında keçi olanı açtı. Seçiminizi kalan diğer kapıyla değiştirirseniz kazanma olasılığınız nasıl değişir?",
            options = listOf("%33'ten %50'ye çıkar", "%50'den %33'e düşer", "%33'ten %67'ye çıkar", "Olasılık değişmez"),
            correctAnswer = "%33'ten %67'ye çıkar",
            hint = "Monty Hall probleminde başlangıçtaki seçiminizin doğru olma olasılığı 1/3'tür. Seçimi değiştirdiğinizde olasılık 2/3'e (%67) yükselir."
        ),
        Puzzle(
            id = 26,
            title = "Doğrucu ve Yalancı Mantığı",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Bir adada sadece her zaman doğruyu söyleyenler ve her zaman yalan söyleyenler yaşamaktadır. Bir ada yerlisine 'Sen yalancı mısın?' sorusu sorulduğunda ne yanıt verebilir?",
            options = listOf("Evet", "Bazen", "Hayır", "Bilmiyorum"),
            correctAnswer = "Hayır",
            hint = "Doğrucu gerçeği söyleyerek 'Hayır' der. Yalancı ise yalan söylemek zorunda olduğu için gerçeği inkar edip yine 'Hayır' der. Her iki durumda da yanıt 'Hayır'dır."
        ),
        Puzzle(
            id = 30,
            title = "Üç Anahtar ve Üç Ampul",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Kapalı yan odada 3 ampul vardır ve bulunduğunuz odadaki 3 anahtara bağlıdır. Odaya yalnızca BİR KEZ girerek hangi anahtarın hangi ampulü yaktığını kesin olarak nasıl anlarsınız?",
            options = listOf("Sigortayı kapatıp beklemek", "Anahtarı açıp ısı farkını kontrol etmek", "Odaya girip anahtarları denemek", "Tüm anahtarları aynı anda açmak"),
            correctAnswer = "Anahtarı açıp ısı farkını kontrol etmek",
            hint = "1. anahtarı açıp 10 dk bekletip kapatın; 2. anahtarı açık bırakın. Odaya girdiğinizde: Yanan ampul 2. anahtara, sönük ama sıcak ampul 1. anahtara, soğuk ampul 3. anahtara aittir."
        ),
        Puzzle(
            id = 34,
            title = "Kumaş Kesim Problemi",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Bir terzi 60 metrelik bir kumaş topunu her gün 2 metrelik parçalar halinde kesmektedir. Terzinin kumaş topunu tamamen parçalara ayırması kaç gün sürer?",
            options = listOf("30 Gün", "29 Gün", "28 Gün", "31 Gün"),
            correctAnswer = "29 Gün",
            hint = "29. günün sonunda yapılan 29. kesimle birlikte kalan son parça zaten 2 metredir ve kendiliğinden ayrılmış olur. Toplam 29 gün sürer."
        ),
        Puzzle(
            id = 38,
            title = "Yumurta Sepeti ve EKOK",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Bir sepetteki yumurtalar ikişerli, üçerli ve dörderli sayıldığında her seferinde 1 yumurta artmaktadır. Beşerli sayıldığında ise tam gelmektedir. Sepette en az kaç yumurta vardır?",
            options = listOf("21", "35", "25", "45"),
            correctAnswer = "25",
            hint = "2, 3 ve 4'ün EKOK'u 12'dir. Katları: 12k + 1. (13, 25, 37...). 25 sayısı 5'in tam katıdır ve tüm koşulları sağlar."
        ),
        Puzzle(
            id = 42,
            title = "Kardeş Sayısı Denklemi",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Bir ailedeki erkek çocuk Ahmet 'Erkek kardeşlerimin sayısı ile kız kardeşlerimin sayısı eşittir' diyor. Kız kardeş Ayşe ise 'Erkek kardeşlerimin sayısı, kız kardeşlerimin sayısının 2 katıdır' diyor. Bu ailede toplam kaç çocuk vardır?",
            options = listOf("5", "6", "8", "7"),
            correctAnswer = "7",
            hint = "Ailede 4 erkek, 3 kız çocuk vardır (Toplam 7). Ahmet için 3 erkek = 3 kız; Ayşe için 4 erkek = 2 kızın 2 katıdır."
        ),
        Puzzle(
            id = 46,
            title = "Tek Hat Ray Çelişkisi",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Tek hatlı dar bir tünelde tek bir tren rayı vardır. İki tren aynı ray üzerinde aynı gün saat 14:00'te birbirine doğru son hızla tünelden geçmiş fakat çarpışmamışlardır. Bu nasıl mümkündür?",
            options = listOf("Farklı saatlerde geçmişlerdir", "Tünel çift katlıdır", "Geri geri gitmişlerdir", "Tünel çok uzundur"),
            correctAnswer = "Farklı saatlerde geçmişlerdir",
            hint = "Biri öğleden sonra 14:00'te, diğeri ise gece yarısı 02:00'de veya farklı saat diliminde tünelden geçmiştir."
        ),
        Puzzle(
            id = 50,
            title = "Terazi ile Sahte Para Tespiti",
            category = PuzzleCategory.LOGIC,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Önünüzde görünüşte aynı 8 madeni para vardır. Biri sahtedir ve diğerlerinden daha HAFİFTİR. İki kefeli denge terazisi kullanarak en fazla kaç tartımda sahte parayı kesin olarak bulabilirsiniz?",
            options = listOf("1 Tartım", "4 Tartım", "3 Tartım", "2 Tartım"),
            correctAnswer = "2 Tartım",
            hint = "1. Tartım: Paraları 3-3-2 ayırın. 3'e 3 tartın. Hafif gelen 3'lüden (veya dengede kalırsa kalan 2'liden) 2. tartımda 1'e 1 tartarak sahte para kesinlikle bulunur."
        ),

        // ==========================================
        // MEMORY (Levels 3, 7, 11, 15, 19, 23, 27, 31, 35, 39, 43, 47)
        // ==========================================
        Puzzle(
            id = 3,
            title = "Stroop Renk Algısı Testi",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.STROOP_COLOR,
            question = "MAVİ",
            options = listOf("Mavi", "Kırmızı", "Sarı", "Yeşil"),
            correctAnswer = "Yeşil",
            hint = "Yazının anlamına aldanmayın. Yazının yazıldığı mürekkep rengi Yeşil'dir.",
            extraData = "0xFF4CAF50" // Green color
        ),
        Puzzle(
            id = 7,
            title = "Görsel Matris Hafızası (3x3)",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.GRID_MEMORY,
            question = "Ekranda yanan 3 karonun konumunu aklınızda tutun ve süre bitince aynı karolara dokunun!",
            correctAnswer = "0,4,8",
            hint = "Sol üstten sağ alta doğru köşegen çizgisini aklınızda tutun (0, 4 ve 8. kareler).",
            extraData = "3"
        ),
        Puzzle(
            id = 11,
            title = "Kısa Süreli Sayı Hafızası",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.NUMBER_MEMORY,
            question = "Ekranda 3 saniye boyunca belirecek 4 basamaklı sayıyı hafızanıza kaydedin ve süre bitince girin!",
            correctAnswer = "7492",
            hint = "Zihninizde tekrarlayın: 74 - 92 (yetmiş dört, doksan iki).",
            extraData = "7492"
        ),
        Puzzle(
            id = 15,
            title = "Stroop Renk Dikkat Testi II",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.STROOP_COLOR,
            question = "KIRMIZI",
            options = listOf("Sarı", "Kırmızı", "Mavi", "Yeşil"),
            correctAnswer = "Sarı",
            hint = "Yazı 'KIRMIZI' yazıyor ama hangi renk mürekkeple ekrana basılmış? (Cevap: Sarı).",
            extraData = "0xFFFFEB3B" // Yellow color
        ),
        Puzzle(
            id = 19,
            title = "Görsel Desen Hafızası (Artı Şekli)",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.GRID_MEMORY,
            question = "3x3 karede yanan 4 karonun oluşturduğu artı (+) desenini hafızanıza kazıyın!",
            correctAnswer = "1,3,5,7",
            hint = "Üst, sol, sağ ve alt merkezdeki 4 karoya odaklanın.",
            extraData = "3"
        ),
        Puzzle(
            id = 23,
            title = "5 Haneli Sayı Hafızası",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.NUMBER_MEMORY,
            question = "Ekranda gösterilen 5 haneli sayıyı zihninizde tutun ve süre bitince tuşlayın!",
            correctAnswer = "91823",
            hint = "Gruplayarak ezberleyin: 9 - 18 - 23.",
            extraData = "91823"
        ),
        Puzzle(
            id = 27,
            title = "Stroop Renk Çeldirici III",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.STROOP_COLOR,
            question = "SİYAH",
            options = listOf("Siyah", "Mavi", "Kırmızı", "Turuncu"),
            correctAnswer = "Mavi",
            hint = "Kelimenin yazıldığı görsel renk Mavi'dir.",
            extraData = "0xFF2196F3" // Blue color
        ),
        Puzzle(
            id = 31,
            title = "X Deseni Matris Hafızası",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.GRID_MEMORY,
            question = "3x3 matriste yanan 5 karonun yerini hafızanızda tutun ve süre bitince aynı karolara dokunun!",
            correctAnswer = "0,2,4,6,8",
            hint = "4 köşe karo ve tam merkezdeki karo (X harfi deseni).",
            extraData = "3"
        ),
        Puzzle(
            id = 35,
            title = "6 Haneli Sayı Hafızası",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.NUMBER_MEMORY,
            question = "Ekranda gösterilen 6 haneli sayıyı hafızanıza kaydedin ve süre bitince girin!",
            correctAnswer = "382506",
            hint = "İkili gruplar halinde okuyun: 38 - 25 - 06.",
            extraData = "382506"
        ),
        Puzzle(
            id = 39,
            title = "Stroop Renk Çeldirici IV",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.STROOP_COLOR,
            question = "TURUNCU",
            options = listOf("Mor", "Sarı", "Yeşil", "Turuncu"),
            correctAnswer = "Yeşil",
            hint = "Kelimenin anlamına değil, yeşil renkli görünümüne odaklanın.",
            extraData = "0xFF4CAF50" // Green color
        ),
        Puzzle(
            id = 43,
            title = "4x4 Büyük Matris Hafızası",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.GRID_MEMORY,
            question = "4x4 matriste yanan 5 karonun konumlarını aklınızda tutun ve süre bitince dokunun!",
            correctAnswer = "2,5,8,11,14",
            hint = "Çapraz ve simetrik konumlanan karoları aklınızda tutun.",
            extraData = "4"
        ),
        Puzzle(
            id = 47,
            title = "7 Haneli İleri Düzey Hafıza",
            category = PuzzleCategory.MEMORY,
            type = PuzzleType.NUMBER_MEMORY,
            question = "Ekranda beliren 7 basamaklı zorlu sayıyı aklınızda tutun ve süre bitince tuşlayın!",
            correctAnswer = "8514930",
            hint = "Gruplama tekniği: 851 - 49 - 30.",
            extraData = "8514930"
        ),

        // ==========================================
        // WORD (Levels 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48)
        // ==========================================
        Puzzle(
            id = 4,
            title = "Bilişsel Terim Anagramı",
            category = PuzzleCategory.WORD,
            type = PuzzleType.WORD_UNSCRAMBLE,
            question = "Aşağıdaki karışık harfleri doğru sırayla birleştirerek soyut düşünme, çıkarım yapma ve doğru akıl yürütme disiplinini ifade eden 6 harfli kelimeyi yazınız:\n\nM - A - N - T - I - K",
            correctAnswer = "MANTIK",
            hint = "Doğru düşünme sanatı ve akıl yürütme yöntemi."
        ),
        Puzzle(
            id = 8,
            title = "Meslek ve Üretim Analojisi",
            category = PuzzleCategory.WORD,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Kavramsal analojiyi tamamlayınız:\n\n'MİMAR' yapının 'PLAN'ını çizerse; 'BESTECİ' müziğin nesini yazar?",
            options = listOf("Nota", "Şiir", "Söz", "Beste"),
            correctAnswer = "Nota",
            hint = "Mimar yapıyı plan ile kağıda döker, besteci ise müziği notalarla kayda geçirir."
        ),
        Puzzle(
            id = 12,
            title = "Zihinsel Eylem Anagramı",
            category = PuzzleCategory.WORD,
            type = PuzzleType.WORD_UNSCRAMBLE,
            question = "Karışık verilen harflere sırayla dokunarak bilginin zihinde işlenmesi ve kavranması eylemini (6 harfli) oluşturunuz:\n\nK - A - V - R - A - M",
            correctAnswer = "KAVRAM",
            hint = "Bir nesnenin veya düşüncenin zihindeki soyut tasarımı ve genel adı."
        ),
        Puzzle(
            id = 16,
            title = "Zıt Anlamlı Sözcükler",
            category = PuzzleCategory.WORD,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "'MÜSRİF' (aşırı savurgan) sözcüğünün karşıtı 'TUTUMLU' ise, 'CÖMERT' sözcüğünün doğrudan zıt anlamlısı nedir?",
            options = listOf("Fakir", "Zengin", "Cimri", "Borçlu"),
            correctAnswer = "Cimri",
            hint = "Karşılıksız veren ve eli açık olan cömert kimsenin zıttı, elini sıkı tutan 'cimri'dir."
        ),
        Puzzle(
            id = 20,
            title = "Düşünce Disiplini Anagramı",
            category = PuzzleCategory.WORD,
            type = PuzzleType.WORD_UNSCRAMBLE,
            question = "Harfleri doğru sırada seçerek varlığı ve bilgiyi sorgulayan düşünce disiplinini (7 harfli) yazınız:\n\nF - E - L - S - E - F - E",
            correctAnswer = "FELSEFE",
            hint = "Varlığı, bilgiyi ve ahlakı akıl yoluyla sorgulayan düşünce bilimi."
        ),
        Puzzle(
            id = 24,
            title = "Ölçüm Aleti Analojisi",
            category = PuzzleCategory.WORD,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Kavramsal ölçüm ilişkisini tamamlayınız:\n\n'TERMOMETRE' ortamdaki 'SICAKLIK' değerini ölçüyorsa, 'BAROMETRE' neyi ölçer?",
            options = listOf("Rüzgar", "Sıcaklık", "Nem", "Basınç"),
            correctAnswer = "Basınç",
            hint = "Barometre, açık hava atmosfer basıncını ölçen bilimsel alettir."
        ),
        Puzzle(
            id = 28,
            title = "Bilişsel Yetenek Anagramı",
            category = PuzzleCategory.WORD,
            type = PuzzleType.WORD_UNSCRAMBLE,
            question = "Harfleri doğru sırada seçerek bilgiyi akılda tutma ve saklama yetisini (6 harfli) oluşturunuz:\n\nH - A - F - I - Z - A",
            correctAnswer = "HAFIZA",
            hint = "Bellek, öğrenilen bilgileri zihinde saklayabilme gücü."
        ),
        Puzzle(
            id = 32,
            title = "Parça-Bütün Analojisi",
            category = PuzzleCategory.WORD,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "Kavramsal parça-bütün hiyerarşisini tamamlayınız:\n\n'AĞAÇ' için 'ORMAN' ne ise, 'KUM' tanesi için o nedir?",
            options = listOf("Taş", "Kaya", "Toprak", "Çöl"),
            correctAnswer = "Çöl",
            hint = "Çok sayıda ağaç birleşerek ormanı, sayısız kum tanesi birleşerek çölü oluşturur."
        ),
        Puzzle(
            id = 36,
            title = "Zeka Kavramı Anagramı",
            category = PuzzleCategory.WORD,
            type = PuzzleType.WORD_UNSCRAMBLE,
            question = "Karışık harfleri birleştirerek problem çözme ve yeni durumlara uyum sağlama yeteneğini (6 harfli) yazınız:\n\nB - E - L - L - E - K",
            correctAnswer = "BELLEK",
            hint = "Yaşananları, öğrenilenleri ve bunların geçmişle ilgisini bilinçte saklama gücü."
        ),
        Puzzle(
            id = 40,
            title = "Eş ve Zıt Anlam Muhakemesi",
            category = PuzzleCategory.WORD,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "'ÖZGÜN' (orijinal, kendine has yaratıcı) niteliğinin doğrudan zıt anlamlısı aşağıdakilerden hangisidir?",
            options = listOf("Eski", "Basit", "Taklit", "Zayıf"),
            correctAnswer = "Taklit",
            hint = "Özgün olan eser yaratıcıdır; özgün olmayıp başkasından kopyalanan eser ise 'taklit'tir."
        ),
        Puzzle(
            id = 44,
            title = "Gözlem Aracı Anagramı",
            category = PuzzleCategory.WORD,
            type = PuzzleType.WORD_UNSCRAMBLE,
            question = "Harfleri doğru sırayla birleştirerek uzaydaki gök cisimlerini incelemeye yarayan optik aletin adını (8 harfli) yazınız:\n\nT - E - L - E - S - K - O - P",
            correctAnswer = "TELESKOP",
            hint = "Yıldızları, gezegenleri ve gök cisimlerini gözlemlemek için kullanılan optik aygıt."
        ),
        Puzzle(
            id = 48,
            title = "Optik Gözlem Analojisi",
            category = PuzzleCategory.WORD,
            type = PuzzleType.MULTIPLE_CHOICE,
            question = "İşlevsel analojiyi tamamlayınız:\n\n'MİKROSKOP' gözle görülmeyen çok 'KÜÇÜK' cisimleri büyütüyorsa; 'TELESKOP' ne tür cisimleri inceler?",
            options = listOf("Hızlı", "Uzak", "Parlak", "Eski"),
            correctAnswer = "Uzak",
            hint = "Mikroskop mikro boyutlardaki küçük varlıkları, teleskop ise ışık yılı mesafelerdeki 'uzak' gök cisimlerini yakınlaştırır."
        )
    ).sortedBy { it.id }
}

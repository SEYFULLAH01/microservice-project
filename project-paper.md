# Abstract
Bu proje, mikroservis mimarisinde hata toleransını yönetmek amacıyla "Circuit Breaker" (Devre Kesici) tasarım deseninin Spring Boot, Netflix Eureka ve Hystrix (veya Resilience4j) kullanılarak nasıl uygulanacağını araştırmakta ve geliştirmektedir. Sistem en az 3 farklı mikroservisi içerecek şekilde tasarlanmış ve Docker ortamına aktarılarak izole bir şekilde çalıştırılması sağlanmıştır.

# 1. Introduction
Mikroservis sistemlerinde servisler birbirleriyle ağ üzerinden haberleşirler. Bir servisteki gecikme veya çökme, diğer servisleri tıkayarak sistem genelinde büyük arızalara (cascading failures) yol açabilir. "1. Group" projesi kapsamında bu sorunu engellemek için Circuit Breaker mekanizması implemente edilmiştir.

# 2. Background
- **Spring Boot:** Bağımsız ve üretime hazır Spring uygulamaları oluşturmayı sağlayan framework.
- **Netflix Eureka:** Servis keşfi (Service Discovery) işlemlerini yürüten sistem.
- **Hystrix / Resilience4j:** Gecikme ve hata toleransını yöneten Circuit Breaker kütüphaneleri.
- **Docker:** Uygulamaları bağımlılıklarından izole ederek konteyner ortamında tutarlı çalışmalarını sağlayan platform.

# 3. Development Details (System Architecture)
Geliştirilen sistem 3 adet mikroservisten oluşmaktadır:
1. **Eureka Server (Service Registry):** Diğer tüm servislerin kendini kaydettiği ve birbirlerinin adreslerini bulduğu ana kayıt servisi.
2. **Product Service:** İstekleri karşılayan temel ve bağımsız bir mikroservis. Devre kesicinin test edilebilmesi için zaman zaman gecikmeli (delay) yanıtlar dönmek veya hata fırlatmak üzere simüle edilmiştir.
3. **Order Service (Circuit Breaker Enabled):** Product Service'i çağıran servis. Eğer Product Service belirtilen süre zarfında cevap dönmezse veya hata üretirse, *Circuit Breaker (Hystrix)* devreye girerek "Fallback" metoduna yönlenir ve kullanıcının veya sistemin donmasını engeller.

**Deployment:**
Uygulamalar Docker imajı olarak derlenmiş, `docker-compose.yml` kullanılarak tek bir komutla (`docker-compose up`) tüm servislerin birbiriyle entegre şekilde (aynı bridge network üzerinde) çalıştırılması sağlanmıştır.

# 4. Kesinti Senaryoları ve Zincirleme Hataların (Cascading Failures) Önlenmesi
Sistemde bir mikroservisin kasıtlı olarak kapatılması veya çökmesi durumunda sistemin vereceği tepkiler aşağıdaki gibi tasarlanmıştır:

1. **Product Service'in Kapanması Durumu:**
Normal şartlarda Order Service, çökmüş olan Product Service'den cevap beklerken tüm "thread"leri (işlem parçacıkları) kilitlenir ve artan yük ile birlikte Order Service de çöker (Cascading Failure). Ancak projemizde uygulanan **Resilience4j Circuit Breaker** sayesinde; Product Service kapandığında Devre Kesici "Açık (Open)" duruma geçer. Order Service kilitlenmek yerine anında "Fallback" (Yedek) metodunu devreye sokar. Sistemin geri kalanı kusursuz çalışmaya devam eder ve Order Service ayakta kalır.

2. **Eureka Server'ın Kapanması Durumu:**
Mevcut istemciler (Product ve Order), Eureka'dan aldıkları adres kayıtlarını (registry) kendi içlerinde "Cache" (önbellek) olarak tutarlar. Eureka çökse dahi, mikroservisler kısa bir süre daha kendi aralarında önbellekteki IP'ler üzerinden haberleşmeye devam edebilirler.

# 5. Conclusion
Bu projede, mikroservis mimarisinin en temel problemlerinden biri olan "Cascading Failures" (zincirleme hatalar) senaryosu başariyla simüle edilmiş ve engellenmiştir. Resilience4j kullanılarak uygulanan Circuit Breaker sayesinde, çöken veya yanıt veremeyen ürün servisi (Product Service), sipariş servisini (Order Service) kilitlememiş; bunun yerine sistem anında yedek (fallback) senaryosuna geçerek istemciye kontrollü bir hata mesajı ("Üzgünüz, Ürün Servisinde bir arıza var...") dönmüştür. Tüm bu altyapı Docker ortamında izole edilerek tam bağımsız bir sistem elde edilmiştir.

# 5. Ekler (Sistem Çıktıları ve Ekran Görüntüleri)
Ödev tesliminde sistemin çalıştığını kanıtlayan ekran görüntüleri:

**Görsel 1: Docker Konteynerlerinin Çalışma Durumu (Terminal veya Docker Desktop)**
*(Buraya Docker'da 3 servisin de çalıştığını gösteren ekran görüntüsünü ekleyin)*

**Görsel 2: Eureka Service Registry (localhost:8761)**
*(Buraya `PRODUCT-SERVICE` ve `ORDER-SERVICE`'in Eureka paneline başarıyla kaydolduğunu gösteren ilk attığınız ekran görüntüsünü ekleyin)*

**Görsel 3: Circuit Breaker Fallback Devrede (localhost:8082/orders)**
*(Buraya en son elde ettiğimiz "Sistem çökmedi." yazan hata toleransı ekran görüntüsünü ekleyin)*

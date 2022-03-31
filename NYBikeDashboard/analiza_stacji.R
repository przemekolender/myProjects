library(stringi)
library(shiny)
library(leaflet)
library(dplyr)
library(ggplot2)

#--------------------------Wczytanie danych------------------------------------

options(stringsAsFactors = FALSE)

sty <- read.csv("201801-citibike-tripdata.csv")
lut <- read.csv("201802-citibike-tripdata.csv")
mar <- read.csv("201803-citibike-tripdata.csv")
kwi <- read.csv("201804-citibike-tripdata.csv")
maj <- read.csv("201805-citibike-tripdata.csv")
cze <- read.csv("201806-citibike-tripdata.csv")

all.data <- rbind(sty, lut, mar, kwi, maj, cze)

rm(sty, lut, mar, kwi, maj, cze)


#--------Utworzenie ramki ranych na której będzie pracowac aplikacja-----------

# Wybranie potrzebnych kolumn
app.database <-  all.data %>% 
  select(starttime, stoptime,
         start.station.name, start.station.latitude, start.station.longitude, 
         end.station.name, end.station.latitude, end.station.longitude) 


# Dodanie kolumny z data wypożyczenia
app.database <- app.database %>% 
  mutate(start.day = as.Date(stri_sub(starttime, 1, length = 10)))

# Dodanie kolumny z godzina wypozyczenia
app.database <- app.database %>% 
  mutate(start.hour = as.integer(stri_extract_first_regex(starttime, " \\d{2}")))

# Dodanie kolumny z data oddania
app.database <- app.database %>% 
  mutate(end.day = as.Date(stri_sub(stoptime, 1, length = 10)))

# Dodanie kolumny z godzina oddania
app.database <- app.database %>% 
  mutate(end.hour = as.integer(stri_extract_first_regex(stoptime, " \\d{2}")))

# znow wybranie odpowiednich kolumn
app.database <- app.database %>% select(start.station.name, 
                                        start.station.latitude, 
                                        start.station.longitude, 
                                        end.station.name, 
                                        end.station.latitude, 
                                        end.station.longitude,
                                        start.day, start.hour, 
                                        end.day, end.hour)


#----------wczytanie/zapisane app.database-------------

#write.csv(app.database, file = "AppDatabase.csv")
#app.database <- read.csv("AppDatabase.csv")


#------------------- dodatkowe obliczenia, niepotrzebne do aplikacji-------------


# znalezienie dancyh o wypozyczeniach ze stacji w Montrealu
montreal <- all.data %>% 
  filter(start.station.latitude > 41) %>% 
  select(tripduration, start.station.name, starttime, end.station.name, stoptime)


#znalezienie stacji z najmniejsza liczba wypozyczen
sta <- all.data %>%
  group_by(start.station.name) %>%  
  summarise(n = n()) %>% arrange(n)

#dobranie odpowiednich kolumn do znalezionej stacji
x <- all.data %>%
  filter(start.station.name == "8D OPS 01" |
           start.station.name == "E 3 St & 1 Ave" | 
           start.station.name == "NYCBS Depot - PIT") %>% 
  select(tripduration, starttime, start.station.name)

#znalezienie zstacji z najmniejszą liczba zwrotów
sto <- all.data %>%
  group_by(end.station.name) %>%  
  summarise(n = n()) %>% arrange(n)

#dobranie odpowiednich kolumn do znalezionej stacji
y <- all.data %>% filter(end.station.name %in% sto$end.station.name[1:11]) %>% 
  select(tripduration, stoptime, end.station.name)

# sprawdzenie ile stacji zaczuna sie na 8D
eightD <- all.data %>% 
  mutate(k = stri_extract_first_regex(start.station.name, "8D")) %>% 
  filter(!is.na(k))





#----------- APLIKACJA-------------------------------------------------

#-----------------ui--------------------------

ui <- bootstrapPage(
  
  #Ustawienia grafiki panelu menu
  tags$style(type = "text/css", "#controls{background-color: white;padding: 0 20px 20px 20px;cursor: move;}"),
  
  #mapa rysowana na calej stronie
  leafletOutput("mapPlot", height = "700px"),
  
  #ustawienia panela menu
  absolutePanel(
    id = "controls",
    top = 10, bottom = "auto", 
    left = 40, right = "auto", 
    width = 310, height = "auto",
    
    #tytul
    h3("MENU"),
    
    # interakrtwne elementy
    selectInput("type", "Rodzaj interakcji:",
                choices = c("wypozyczenia", "zwroty", "wszystkie")),
    
    
    dateRangeInput("date", "Przedział czasowy",
                   start = "2018-01-01",
                   end = "2018-06-30",
                   min = "2018-01-01",
                   max = "2018-06-30"),
    
    sliderInput("hour", "Przedział godzinowy",
                min = 0:00, max = 24,round = TRUE, 
                step = 1, value = c(3, 6, 9, 12, 15, 18, 21)),
    
    actionButton("go", "Wyświetl"),
    
    #wykres na panelu
    plotOutput("bplot", width = 280, height = 300)
  )
)


#------------serwer------------------------


server <- function(input, output){
  
  observeEvent(input$go,{
    
    choice <- input$type
    
    start.date <- input$date[1]
    stop.date <- input$date[2]
    
    start.hour <- input$hour[1]
    stop.hour <- input$hour[2]
    
    # stworzenie zakresu dni, ktore zbadamy
    #zabezpieczenie, jesli ktos poda pierwsza date pozniejsza niz druga,
    #to i tak badany jest przedzial miedzy nimi
    ifelse(start.date <= stop.date, 
           day.array <- seq(start.date, stop.date, by = 1), 
           day.array <- seq(stop.date, start.date, by = 1))
    
    # stworzenie zakresu godzin, ktore zbadamy
    hour.array <- start.hour:(stop.hour - 1)
    
    #jesli wybieramy zroty i wyporzyczenia
    # funkcja tworzy tabele zwrotow i wypozyczen i nastepnie je laczy
    if(choice == "wszystkie"){
      
      #stworzenie tabeli wypozyczen
      start <- app.database %>% 
        select(start.station.name,
               start.station.latitude, start.station.longitude, start.day, start.hour)
      
      names(start) <- c("name", "latitude", "longitude", "day", "hour")
      
      #zawezenie do wybranych dat
      start <- start %>% filter(day %in% day.array) %>% 
        filter(hour %in% hour.array) %>% 
        select(name, latitude, longitude)
      
      #podliczenie interakcji
      start <- start %>% 
        group_by(name, latitude, longitude) %>% 
        summarise(n = n())
      
      #stworzenie tabeli zwrotow
      stop <- app.database %>% 
        select(end.station.name,
               end.station.latitude, end.station.longitude, end.day, end.hour)
      
      names(stop) <- c("name", "latitude", "longitude", "day", "hour")
      
      #zawezenie do wybranych dat
      stop <- stop %>% filter(day %in% day.array) %>% 
        filter(hour %in% hour.array) %>% 
        select(name, latitude, longitude)
      
      #podliczenie interakcji
      stop <- stop %>% 
        group_by(name, latitude, longitude) %>% 
        summarise(n = n())
      
      #polaczenie tabel
      df <- start %>% 
        full_join(stop, by = ("name" = "name"))
      
      #utoworzenie kolum zeby nie miec wartosci na
      df <- df %>% 
        mutate(latitude = ifelse(is.na(latitude.x), latitude.y, latitude.x))
      
      df <- df %>% 
        mutate(longitude = ifelse(is.na(longitude.y), longitude.x, longitude.y))
      
      #pozbycie sie wartosci NA w liczbie interkacji
      df <- df %>% 
        mutate(n.x = ifelse(is.na(n.x), 0, n.x)) %>% 
        mutate(n.y = ifelse(is.na(n.y), 0, n.y))
      
      #zsumowanie interakcji
      df <- df %>% 
        mutate(n = n.x + n.y)
      
      df <- ungroup(df)
      
      #wybranie potrzebnych kolumn
      df <- df %>% 
        select(name, latitude, longitude, n)
      
      
      
    }else{
      
      #jesli wybrane zostana wypozyczenia to tworzy sie tylko 1 tabela
      if(choice == "wypozyczenia"){
        
        df <- app.database %>% 
          select(start.station.name,
                 start.station.latitude, start.station.longitude,
                 start.day, start.hour)
        
        names(df) <- c("name", "latitude", "longitude", "day", "hour")
        
      }else{
        #jesli wybrane zostana zwroty to tworzy sie tylko 1 tabela
        df <- app.database %>% 
          select(end.station.name,
                 end.station.latitude, end.station.longitude,
                 end.day, end.hour)
        
        names(df) <- c("name", "latitude", "longitude", "day", "hour")
      }
      
      #zawezenie przedzialu
      df <- df %>% 
        filter(day %in% day.array) %>% 
        filter(hour %in% hour.array)
      
      #podliczenie interakcji
      df <- df %>% 
        group_by(name, latitude, longitude) %>% 
        summarise(n = n())
    }  
    
    
    output$mapPlot <- renderLeaflet({
      #stworzenie palety kolorow do mapy
      colors <- c("green", "red")
      pal <- colorFactor(colors, df$n)
      
      #dodanie kolumny z informaja do wyswietleina
      df <- df %>% 
        mutate(info = paste(name,"<br/>", n))
      
      #generowanie mapy
      leaflet() %>% addTiles() %>% addCircleMarkers(data = df, 
                                                    lat = ~latitude, 
                                                    lng = ~longitude,
                                                    radius = ~5,
                                                    weight = 8.5,
                                                    popup = ~info,
                                                    color = ~pal(n),
                                                    opacity = 0.8) %>% 
        setView(lng = -73.999, lat = 40.737, zoom = 11.5)
    })
    
    #wybor 10 najbardziej uzywanych stacji
    df <- df %>% arrange(desc(n))
    most.used <- df[1:10, ]
    most.used <- ungroup(most.used)
    
    #dodanie nowej kolumny z informacja
    most.used <- most.used %>% mutate(info = paste(name,"<br/>", n))  
    
    #dorysowanie fioletowych markerow
    leafletProxy("mapPlot", data = most.used) %>% addCircleMarkers(lat = ~latitude, 
                                                                   lng = ~longitude,
                                                                   radius = ~6,
                                                                   popup = ~info,
                                                                   color = "purple",
                                                                   opacity = 0.6)
    
    #dodanie nowej kolumny, zeby ggplot dobrze sortowal dane
    m = 10:1
    most.used <- most.used %>% select(name, n) %>% mutate(des = paste(name, n, sep = ", ")) 
    most.used <- cbind(most.used, m)
    
    #rysowanie wykresu na panelu
    output$bplot <- renderPlot({
      ggplot(data = most.used, aes(x = reorder(des, m), y = n)) + 
        labs(title = "10 najpopularniejszych stacji") +
        geom_bar(stat = "identity", fill = "steelblue") + 
        coord_flip() + 
        theme_minimal() + 
        geom_text(aes(label = des), color = "black", position = position_fill(), hjust = 0) +
        theme(axis.title.x = element_blank(), axis.title.y = element_blank(), axis.text.y = element_blank()) 
      
    })
  })
}


#------------uruchomienie aplikacji-----------------------

shinyApp(ui = ui, server = server)
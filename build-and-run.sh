#!/bin/bash
echo "Docker ile projeler derleniyor ve ayağa kaldırılıyor..."
docker compose build
docker compose up -d
echo "Bitti! Servisler şu an çalışıyor."

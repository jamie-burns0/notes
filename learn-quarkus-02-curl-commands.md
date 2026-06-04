```
curl -X GET --url http://localhost:8080/collection/artists \
-H "Content-Type: application/json"

curl -X GET --url http://localhost:8080/collection/works \
-H "Content-Type: application/json"

curl -X GET --url http://localhost:8080/collection/galleries \
-H "Content-Type: application/json"

curl -X GET --url http://localhost:8080/collection/artists/1/works \
-H "Content-Type: application/json"

curl -X GET --url http://localhost:8080/collection/galleries/2/works \
-H "Content-Type: application/json"

  '{"name":"Pablo Picasso", "origin":"Spain", "period":"20th Century", "knownAs": "Picasso"}'
  '{"name":"Salvador Dali", "origin":"Spain", "period":"20th Century", "knownAs": "Dali"}'

artists=(
  '{"name":"Pablo Picasso", "origin":"Spain", "period":"20th Century", "knownAs": "Picasso"}'
  '{"name":"Salvador Dali", "origin":"Spain", "period":"20th Century", "knownAs": "Dali"}'
  '{"name":"Alfred Wallis", "origin":"England", "period":"20th Century", "knownAs": "Wallis"}'
  '{"name":"L. S. Lowry", "origin":"England", "period":"20th Century", "knownAs": "Lowry"}'
)

for artist in "${(@)artists}"; do
  curl -X POST --url http://localhost:8080/collection/artists -H 'Content-Type: application/json' -d "$artist"
done


HEADER='Content-Type: application/json'

picasso_id=1
picasso_works=(
  '{"name":"Guernica", "year": 1937}'
  '{"name":"Studio (Pigeons) (Velazquez)", "year": 1957}'
  '{"name":"Woman with hat", "year": 1962}'
  '{"name":"Portrait of Igor Stravinsky", "year": 1920}'
  '{"name":"Guitar on pedestal", "year": 1920}'
)

for work in "${(@)picasso_works}"; do
  curl -X POST --url http://localhost:8080/collection/artists/$picasso_id/works -H "$HEADER" -d "$work"
done


dali_id=2
dali_works=(
  '{"name":"The Persistence of Memory", "year": 1931}'
  '{"name":"Un Chien Andalou (film still)", "year": 1928}'
  '{"name":"The Sense of Speed", "year": 1931}'
  '{"name":"Lobster Telephone", "year": 1938}'
  '{"name":"Dance of Time I", "year": 1984}'
)

for work in "${(@)dali_works}"; do
  curl -X POST --url http://localhost:8080/collection/artists/$dali_id/works -H "$HEADER" -d "$work"
done


wallis_id=3
wallis_works=(
  '{"name": "St Ives", "year": 1928}'
  '{"name": "The Blue Ship", "year": 1934}'
  '{"name": "Houses at St Ives, Cornwall", "year": 1942}'
)

for work in "${(@)wallis_works}"; do
  curl -X POST --url http://localhost:8080/collection/artists/$wallis_id/works -H 'Content-Type: application/json' -d "$work"
done


lowry_id=4
lowry_works=(
  '{"name": "Going to the Mill", "year": 1925}'
  '{"name": "Coming Home from the Mill", "year": 1928}'
  '{"name": "Street Scene", "year": 1937}'
  '{"name": "Portrait of Ann", "year": 1957}'
)

for work in "${(@)lowry_works}"; do
  curl -X POST --url http://localhost:8080/collection/artists/$lowry_id/works -H 'Content-Type: application/json' -d "$work"
done


galleries=(
  '{"name":"Unknown/Private", "city":"Unknown"}'
  '{"name":"Museo Reina Sofía", "city":"Madrid"}'
  '{"name":"Museu Picasso Barcelona", "city":"Barcelona"}'
  '{"name":"Musée Picasso Paris", "city":"Paris"}'
)

for gallery in "${(@)galleries}"; do
  curl -X POST --url http://localhost:8080/collection/galleries -H 'Content-Type: application/json' -d "$gallery"
done

gallery_id=1
works=(
  '{"name":"Guitar on pedestal"}'
)

for work in "${(@)works}"; do
  curl -X POST --url http://localhost:8080/collection/galleries/$gallery_id/works -H "$HEADER" -d "$work"
done

gallery_id=2
works=(
  '{"name":"Guernica"}'
)

for work in "${(@)works}"; do
  curl -X POST --url http://localhost:8080/collection/galleries/$gallery_id/works -H "$HEADER" -d "$work"
done

gallery_id=3
works=(
  '{"name":"Studio (Pigeons) (Velazquez)"}'
  '{"name":"Woman with hat"}'
)

for work in "${(@)works}"; do
  curl -X POST --url http://localhost:8080/collection/galleries/$gallery_id/works -H "$HEADER" -d "$work"
done

gallery_id=4
works=(
  '{"name":"Portrait of Igor Stravinsky", "year": 1920}'
)

for work in "${(@)works}"; do
  curl -X POST --url http://localhost:8080/collection/galleries/$gallery_id/works -H "$HEADER" -d "$work"
done
```
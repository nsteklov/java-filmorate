package ru.yandex.practicum.filmorate.model;

public enum МРАRating {
    G,
    PG,
    PG_13 {
        @Override
        public String toString() {
            return "PG-13";
        }
    },
    R,
    NC_17 {
        @Override
        public String toString() {
            return "NC-17";
        }
    }
}

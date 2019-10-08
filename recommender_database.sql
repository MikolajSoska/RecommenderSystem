CREATE TABLE `books`
(
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `asin` VARCHAR(255),
  `title` VARCHAR(255),
  `cluster` INT,
  `description` LONGTEXT,
  KEY (`title`),
  KEY (`cluster`)
);

CREATE TABLE `reviews`
(
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `book_id` INT,
  `positive` INT,
  `weight` DOUBLE,
  `text` LONGTEXT,
  KEY (`book_id`)
);

CREATE TABLE `word_vectors`
(
  `book_id` INT,
  `word_id` INT,
  `word_value` DOUBLE,
  PRIMARY KEY (`book_id`, `word_id`)
);

ALTER TABLE `reviews` ADD FOREIGN KEY (`book_id`) REFERENCES `books` (`id`);
ALTER TABLE `word_vectors` ADD FOREIGN KEY (`book_id`) REFERENCES `books` (`id`);
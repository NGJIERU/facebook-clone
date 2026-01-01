resource "aws_ecr_repository" "auth_service" {
  name                 = "facebook-auth"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  force_delete = true # For dev environment validation
}

resource "aws_ecr_repository" "user_service" {
  name                 = "facebook-user"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  force_delete = true
}

resource "aws_ecr_repository" "feed_service" {
  name                 = "facebook-feed"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  force_delete = true
}

output "ecr_repository_urls" {
  description = "Map of repository names to repository URLs"
  value = {
    auth = aws_ecr_repository.auth_service.repository_url
    user = aws_ecr_repository.user_service.repository_url
    feed = aws_ecr_repository.feed_service.repository_url
  }
}

resource "aws_subnet" "public_subnet_c" {
  vpc_id = data.aws_vpc.selected.id
  cidr_block              = var.cidr_blocks[var.environment][0]
  map_public_ip_on_launch = "true"
  availability_zone       = "us-east-1c"
  tags = {
    Name = "${var.environment}-pubsub-c"
  }
}

resource "aws_subnet" "public_subnet_d" {
  vpc_id                  = data.aws_vpc.selected.id
  cidr_block              = var.cidr_blocks[var.environment][1]
  map_public_ip_on_launch = "true"
  availability_zone       = "us-east-1d"
  tags = {
    Name = "${var.environment}-pubsub-d"
  }
}

resource "aws_subnet" "private_subnet_c" {
  vpc_id            = data.aws_vpc.selected.id
  cidr_block        = var.cidr_blocks[var.environment][2]
  availability_zone = "us-east-1c"
  tags = {
    Name = "${var.environment}-privsub-c"
  }
}

resource "aws_subnet" "private_subnet_d" {
  vpc_id            = data.aws_vpc.selected.id
  cidr_block        = var.cidr_blocks[var.environment][3]
  availability_zone = "us-east-1d"
  tags = {
    Name = "${var.environment}-privsub-d"
  }
}

resource "aws_route_table" "public_subnet_route" {
  vpc_id = data.aws_vpc.selected.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = data.aws_internet_gateway.gateway.internet_gateway_id
  }
  tags = {
    Name = "${var.environment}-pub-router"
  }
}

resource "aws_route_table_association" "public_subnet_assoc_c" {
  subnet_id      = aws_subnet.public_subnet_c.id
  route_table_id = aws_route_table.public_subnet_route.id
}

resource "aws_route_table_association" "public_subnet_assoc_d" {
  subnet_id      = aws_subnet.public_subnet_d.id
  route_table_id = aws_route_table.public_subnet_route.id
}

resource "aws_eip" "eip" {
  domain = "vpc"
  depends_on = [
    aws_route_table_association.public_subnet_assoc_c
  ]
  tags = {
    Name = "${var.environment}-eip"
  }
}

resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.eip.id
  subnet_id     = aws_subnet.public_subnet_c.id

  tags = {
    Name = "${var.environment}-NAT"
  }
}

resource "aws_route_table" "nat_gateway_route" {
  vpc_id = data.aws_vpc.selected.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat.id
  }

  tags = {
    Name = "${var.environment}-nat-gateway-rt"
  }
}

resource "aws_route_table_association" "private_subnet_assoc_c" {
  subnet_id      = aws_subnet.private_subnet_c.id
  route_table_id = aws_route_table.nat_gateway_route.id
}

resource "aws_route_table_association" "private_subnet_assoc_d" {
  subnet_id      = aws_subnet.private_subnet_d.id
  route_table_id = aws_route_table.nat_gateway_route.id
}

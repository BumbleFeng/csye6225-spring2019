# Instructions to Run the Script
<p>"csye6225-aws-networking-setup.sh" Script will</p>
<ul>
  <li>Create a VPC taking NAME as parameter.</li>
	<li>Create 3 subnets in the VPC, each in different availability zone but in the same region under same VPC.</li>
	<li>Create Internet Gateway resource.</li>
	<li>Attach the Internet Gateway to STACK_NAME-csye6225-vpc VPC.</li>
	<li>Create a public Route Table. Attach all subnets created above to the route table.</li>
	<li>Create a public route in the public route table with destination CIDR block 0.0.0.0/0 and the internet gateway as the target.</li>
	<li>Modify the default security group for VPC to remove existing rules and add new rules to only allow TCP traffic on port 22 and 80 from anywhere.</li>
</ul>
<p>"csye6225-aws-networking-teardown.sh" Script will</p>
<ul>
	<li>Delete the VPC and all networking resources taking VPC as parameter</li>
</ul>

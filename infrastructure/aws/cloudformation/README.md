# Instructions to Run the Script
<p>"csye6225-aws-cf-create-stack.sh" Script will</p>
<ul>
  <li>Create a cloudformation stack taking STACK_NAME as parameter.</li>
	<li>Create a Virtual Private Cloud (VPC) resource called STACK_NAME-csye6225-vpc.</li>
	<li>Create 3 subnets in the VPC, each in different availability zone but in the same region under same VPC.</li>
	<li>Create Internet Gateway resource.</li>
	<li>Attach the Internet Gateway to STACK_NAME-csye6225-vpc VPC.</li>
	<li>Create a public Route Table called STACK_NAME-csye6225-rt. Attach all subnets created above to the route table.</li>
	<li>Create a public route in STACK_NAME-csye6225-rt route table with destination CIDR block 0.0.0.0/0 and the internet gateway as the target.</li>
</ul>
<p>"csye6225-aws-cf-terminate-stack.sh" Script will</p>
<ul>
	<li>Delete the stack and all networking resources taking STACK_NAME as parameter</li>
</ul>
<p>"csye6225-cf-networking.json"</p>
<ul>
	<li>Setup required networking resources. Accept stack name as parameter.</li>
</ul>

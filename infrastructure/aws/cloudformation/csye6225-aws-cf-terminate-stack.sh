echo "Enter Stack Name You Want To Delete: "
read STACK_NAME

StackName=$STACK_NAME-csye6225-vpc

aws cloudformation delete-stack --stack-name $StackName
Status=$(aws cloudformation describe-stacks --stack-name $StackName | grep StackStatus| cut -d'"' -f4)

echo "Please wait..."

i=1
sp="/-\|"
echo -n ' '
while [ -n "$Status" ]
do
    Status=$(aws cloudformation describe-stacks --stack-name $StackName 2>&1 | grep StackStatus| cut -d'"' -f4)
    printf "\b${sp:i++%${#sp}:1}"
done

printf "\b"

echo "DELETE_COMPLETE"

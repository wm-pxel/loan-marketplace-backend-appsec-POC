const lambda_handler = async (event) => {
    console.log('Pre Sign-Up event:', JSON.stringify(event, null, 2));

    // Ensure all users require verification
    event.response.autoConfirmUser = false;

    // Return the updated event
    console.log('Processed event:', JSON.stringify(event, null, 2));
    return event;
};

module.exports = { lambda_handler };
